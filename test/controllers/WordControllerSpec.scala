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

import scala.util.{Failure, Try}

class WordControllerSpec extends PlaySpec with MockitoSugar {
  val mockMessagesApi = new DefaultMessagesApi(
    Environment.simple(),
    Configuration.reference,
    new DefaultLangs(Configuration.reference))
  val mockRepo = mock[WordRepository]

  def buildController(wordsRepo: WordRepository = mockRepo,
                      messagesApi: MessagesApi = mockMessagesApi) = {
    new WordController(messagesApi, wordsRepo)
  }

  val mockWord = Word("hello", "type of greeting")

  "editForm" must {
    "return OK and display an empty word form if no custom definition exists" in {
      val mockRepo = mock[WordRepository]
      when(mockRepo.get(any())).thenReturn(None)
      val result = buildController(mockRepo).editForm(mockWord.word)(FakeRequest())
      status(result) mustBe OK
      val doc = Jsoup.parse(contentAsString(result))
      doc.getElementsByTag("textarea").size mustBe 1
      doc.getElementsByTag("textarea").get(0).text() mustBe ""
    }

    "return OK and display an empty word form if a custom definition exists, but it is blank" in {
      val mockRepo = mock[WordRepository]
      when(mockRepo.get(any())).thenReturn(Some(mockWord.copy(definition = "")))
      val result = buildController(mockRepo).editForm(mockWord.word)(FakeRequest())
      status(result) mustBe OK
      val doc = Jsoup.parse(contentAsString(result))
      doc.getElementsByTag("textarea").size mustBe 1
      doc.getElementsByTag("textarea").get(0).text() mustBe ""
    }

    "return OK and display a prefilled form with the existing custom definition" in {
      val mockRepo = mock[WordRepository]
      when(mockRepo.get(any())).thenReturn(Some(mockWord))
      val result = buildController(mockRepo).editForm(mockWord.word)(FakeRequest())
      status(result) mustBe OK
      val doc = Jsoup.parse(contentAsString(result))
      doc.getElementsByTag("textarea").size mustBe 1
      doc.getElementsByTag("textarea").get(0).text() mustBe mockWord.definition
    }
  }

  "edit" must {
    val mockFormData = Map("definition" -> mockWord.definition)

    "return OK, upsert the word to db and redirect to the newly created word's page" in {
      val mockRepo = mock[WordRepository]
      when(mockRepo.upsert(any(), any())).thenReturn(Try(()))
      val request = FakeRequest().withFormUrlEncodedBody(mockFormData.toSeq: _*)
      val result = buildController(wordsRepo = mockRepo).edit(mockWord.word)(request)
      status(result) mustBe SEE_OTHER
      verify(mockRepo).upsert(mockWord.word, mockFormData("definition"))
      await(result).header.headers("Location") mustBe routes.WordController.get(mockWord.word).url
      flash(result).get("message") mustBe Some(s"""Successfully updated definition of "${mockWord.word}".""")
    }

    "return BAD_REQUEST and display the edit form with errors when an invalid submission is made" in {
      pending
    }

    "return INTERNAL_SERVER_ERROR and redisplay the form with an error when an error occurs during saving" in {
      val mockRepo = mock[WordRepository]
      when(mockRepo.upsert(any(), any())).thenReturn(Failure(new Exception))
      val request = FakeRequest().withFormUrlEncodedBody(mockFormData.toSeq: _*)
      val result = buildController(wordsRepo = mockRepo).edit(mockWord.word)(request)
      status(result) mustBe INTERNAL_SERVER_ERROR
      verify(mockRepo).upsert(mockWord.word, mockFormData("definition"))
      val doc = Jsoup.parse(contentAsString(result))
      doc.getElementById("edit-form") must not be null
      doc.getElementsByClass("alert-danger").size mustBe 1
      doc.getElementsByClass("alert-danger").get(0).text mustBe "Error occurred while saving, please try again."
    }
  }
}
