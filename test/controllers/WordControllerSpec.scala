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
import repositories.WordRepository.DuplicateRecordException

import scala.util.{Failure, Try}

class WordControllerSpec extends PlaySpec with MockitoSugar {
  val mockMessagesApi = new DefaultMessagesApi(
    Environment.simple(),
    Configuration.reference,
    new DefaultLangs(Configuration.reference))
  val mockRepo = mock[WordRepository]

  def buildController(messagesApi: MessagesApi = mockMessagesApi,
                      wordsRepo: WordRepository = mockRepo) = {
    new WordController(messagesApi, wordsRepo)
  }

  "addGet" must {
    "return OK and display the add word form" in {
      val result = buildController().addGet()(FakeRequest())
      status(result) mustBe OK
      val doc = Jsoup.parse(contentAsString(result))
      doc.getElementById("form-word-add") must not be null
    }
  }

  "addPost" must {
    val mockSubmission = Word("hello", "type of greeting")
    val mockFormData = Map("word" -> mockSubmission.word, "definition" -> mockSubmission.definition)

    "return OK, add new word to db and redirect to the newly created word's page" in {
      val mockRepo = mock[WordRepository]
      when(mockRepo.add(any(), any())).thenReturn(Try(()))
      val request = FakeRequest().withFormUrlEncodedBody(mockFormData.toSeq: _*)
      val result = buildController(wordsRepo = mockRepo).addPost()(request)
      status(result) mustBe SEE_OTHER
      verify(mockRepo).add(mockSubmission.word, mockSubmission.definition)
      await(result).header.headers("Location") mustBe routes.WordController.get(mockSubmission.word).url
    }

    "return BAD_REQUEST and display the add word form with errors when an invalid submission is made" in {
      val invalidData = mockFormData - "definition"
      val request = FakeRequest().withFormUrlEncodedBody(invalidData.toSeq: _*)
      val result = buildController().addPost()(request)
      status(result) mustBe BAD_REQUEST
      val doc = Jsoup.parse(contentAsString(result))
      doc.getElementById("form-word-add") must not be null
      doc.getElementsByClass("error").size must be > 0
    }

    "return BAD_REQUEST and display the add word form when the submitted word is already in the db" in {
      val mockRepo = mock[WordRepository]
      when(mockRepo.add(any(), any())).thenReturn(Failure(new DuplicateRecordException("")))
      val request = FakeRequest().withFormUrlEncodedBody(mockFormData.toSeq: _*)
      val result = buildController(wordsRepo = mockRepo).addPost()(request)
      status(result) mustBe BAD_REQUEST
      val doc = Jsoup.parse(contentAsString(result))
      doc.getElementById("form-word-add") must not be null
    }
  }
}
