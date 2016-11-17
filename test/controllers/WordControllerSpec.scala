package controllers

import models.Word
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.i18n.{DefaultLangs, DefaultMessagesApi, MessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import repositories.WordRepository

import scala.util.{Failure, Success, Try}

class WordControllerSpec extends PlaySpec with MockitoSugar {
  val mockMessagesApi = new DefaultMessagesApi(
    Environment.simple(),
    Configuration.reference,
    new DefaultLangs(Configuration.reference))

  def buildController(wordsRepo: WordRepository = buildRepo(),
                      messagesApi: MessagesApi = mockMessagesApi) = {
    new WordController(messagesApi, wordsRepo)
  }

  def buildRepo(getResult: Option[Word] = None, upsertResult: Try[Unit] = Success(())) = {
    val mockRepo = mock[WordRepository]
    when(mockRepo.get(any())).thenReturn(getResult)
    when(mockRepo.upsert(any(), any())).thenReturn(upsertResult)
    mockRepo
  }

  val mockWord = Word("hello", "type of greeting")

  "editForm" must {
    "return 200 and display an empty word form if no custom definition exists" in {
      val result = buildController().editForm(mockWord.word)(FakeRequest())
      status(result) mustBe OK
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("textarea").size mustBe 1
      doc.select("textarea").get(0).text() mustBe ""
    }

    "return 200 and display an empty word form if a custom definition exists, but it is blank" in {
      val mockRepo = buildRepo(getResult = Some(mockWord.copy(definition = "")))
      val result = buildController(mockRepo).editForm(mockWord.word)(FakeRequest())
      status(result) mustBe OK
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("textarea").size mustBe 1
      doc.select("textarea").get(0).text() mustBe ""
    }

    "return 200 and display a prefilled form with the existing custom definition" in {
      val mockRepo = buildRepo(getResult = Some(mockWord))
      val result = buildController(mockRepo).editForm(mockWord.word)(FakeRequest())
      status(result) mustBe OK
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("textarea").size mustBe 1
      doc.select("textarea").get(0).text() mustBe mockWord.definition
    }
  }

  "edit" must {
    val mockFormData = Map("definition" -> mockWord.definition)

    "return 200, upsert the word to db and redirect to the newly created word's page" in {
      val mockRepo = buildRepo()
      val request = FakeRequest().withFormUrlEncodedBody(mockFormData.toSeq: _*)
      val result = buildController(mockRepo).edit(mockWord.word)(request)
      status(result) mustBe SEE_OTHER
      verify(mockRepo).upsert(mockWord.word, mockFormData("definition"))
      await(result).header.headers("Location") mustBe routes.WordController.get(mockWord.word).url
      flash(result).get("message") mustBe Some(s"""Successfully updated definition of "${mockWord.word}".""")
    }

    "return 400 and display the edit form with errors when an invalid submission is made" in {
      pending
    }

    "return 500 and redisplay the form with an error when an error occurs during saving" in {
      val mockRepo = buildRepo(upsertResult = Failure(new Exception))
      val request = FakeRequest().withFormUrlEncodedBody(mockFormData.toSeq: _*)
      val result = buildController(mockRepo).edit(mockWord.word)(request)
      status(result) mustBe INTERNAL_SERVER_ERROR
      verify(mockRepo).upsert(mockWord.word, mockFormData("definition"))
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("#edit-form").size mustBe 1
      doc.select(".alert-danger").size mustBe 1
      doc.select(".alert-danger").get(0).text mustBe "Error occurred while saving, please try again."
    }
  }

  "get" must {
    "return 200 and display the custom definition for the word" in {
      val mockRepo = buildRepo(getResult = Some(mockWord))
      val result = buildController(mockRepo).get(mockWord.word)(FakeRequest())
      status(result) mustBe OK
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("#custom-definition-panel .panel-body").text mustBe mockWord.definition
    }

    "return 200 and display a message stating that no custom definition exists" in {
      val result = buildController().get(mockWord.word)(FakeRequest())
      status(result) mustBe OK
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("#custom-definition-panel .no-definition").size mustBe 1
    }

    "display an alert with a flash message" in {
      val request = FakeRequest().withFlash("message" -> "hello world")
      val result = buildController().get(mockWord.word)(request)
      val doc = Jsoup.parse(contentAsString(result))
      doc.select(".alert-success").size mustBe 1
      doc.select(".alert-success").get(0).text mustBe request.flash.get("message").get
    }
  }
}
