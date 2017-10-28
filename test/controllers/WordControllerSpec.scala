package controllers

import models.Word
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.i18n.{DefaultLangs, DefaultMessagesApi, MessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import repositories.WordRepository
import sources.{WiktionarySource, YandexDictionarySource, YandexTranslateSource}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class WordControllerSpec extends PlaySpec with MockitoSugar {
  val mockMessagesApi = new DefaultMessagesApi(
    Environment.simple(),
    Configuration.reference,
    new DefaultLangs(Configuration.reference))

  def buildController(wordsRepo: WordRepository = buildWordsRepo(),
                      wikiRepo: WiktionarySource = buildWikiRepo(),
                      yandexTransRepo: YandexTranslateSource = buildYandexTranRepo(),
                      yandexDictRepo: YandexDictionarySource = buildYandexDictRepo(),
                      messagesApi: MessagesApi = mockMessagesApi) = {
    new WordController(messagesApi, wikiRepo, yandexTransRepo, yandexDictRepo, wordsRepo)
  }

  def buildWordsRepo(getResult: Option[Word] = None, upsertResult: Try[Unit] = Success(())) = {
    val mockRepo = mock[WordRepository]
    when(mockRepo.get(any())).thenReturn(Future.successful(getResult))
    when(mockRepo.upsert(any())).thenReturn(Future.successful(upsertResult))
    mockRepo
  }

  def buildWikiRepo(getResult: Option[String] = None) = {
    val mockRepo = mock[WiktionarySource]
    when(mockRepo.get(any())).thenReturn(Future.successful(getResult))
    mockRepo
  }

  def buildYandexTranRepo(getResult: Option[String] = None) = {
    val mockRepo = mock[YandexTranslateSource]
    when(mockRepo.get(any())).thenReturn(Future.successful(getResult))
    mockRepo
  }

  def buildYandexDictRepo(getResult: Option[String] = None) = {
    val mockRepo = mock[YandexDictionarySource]
    when(mockRepo.get(any())).thenReturn(Future.successful(getResult))
    mockRepo
  }


  val mockWord = Word(None, "hello", Seq("hey", "type of greeting"))

  "editForm" must {
    "return 200 and display an empty word form if no custom definition exists" in {
      val result = buildController().editForm(mockWord.word)(FakeRequest())
      status(result) mustBe OK
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("textarea").size mustBe 1
      doc.select("textarea").get(0).text() mustBe ""
    }

    "return 200 and display an empty word form if a custom definition exists, but it is blank" in {
      val mockRepo = buildWordsRepo(getResult = Some(mockWord.copy(definitions = Seq())))
      val result = buildController(mockRepo).editForm(mockWord.word)(FakeRequest())
      status(result) mustBe OK
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("textarea").size mustBe 1
      doc.select("textarea").get(0).text() mustBe ""
    }

    "return 200 and display a prefilled form with the existing custom definition" in {
      val mockRepo = buildWordsRepo(getResult = Some(mockWord))
      val result = buildController(mockRepo).editForm(mockWord.word)(FakeRequest())
      status(result) mustBe OK
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("textarea").size mustBe 1
      doc.select("textarea").get(0).text() mustBe mockWord.definitions.mkString("\n")
    }
  }

  "edit" must {
    val mockFormData = Map("definitions" -> mockWord.definitions.mkString("\n"))

    "return 200, upsert the word to db and redirect to the newly created word's page" in {
      val mockRepo = buildWordsRepo()
      val request = FakeRequest().withFormUrlEncodedBody(mockFormData.toSeq: _*)
      val result = buildController(mockRepo).edit(mockWord.word)(request)
      status(result) mustBe SEE_OTHER
      verify(mockRepo).upsert(Word(None, mockWord.word, mockFormData("definitions").split("\n")))
      await(result).header.headers("Location") mustBe routes.WordController.get(mockWord.word).url
      flash(result).get("message") mustBe Some(s"""Successfully updated definition of "${mockWord.word}".""")
    }

    "return 400 and display the edit form with errors when an invalid submission is made" in {
      pending
    }

    "return 500 and redisplay the form with an error when an error occurs during saving" in {
      val mockRepo = buildWordsRepo(upsertResult = Failure(new Exception))
      val request = FakeRequest().withFormUrlEncodedBody(mockFormData.toSeq: _*)
      val result = buildController(mockRepo).edit(mockWord.word)(request)
      status(result) mustBe INTERNAL_SERVER_ERROR
      verify(mockRepo).upsert(Word(None, mockWord.word, mockFormData("definitions").split("\n")))
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("#edit-form").size mustBe 1
      doc.select(".alert-danger").size mustBe 1
      doc.select(".alert-danger").get(0).text mustBe "Error occurred while saving, please try again."
    }
  }

  "get" must {
    "return 200 and display the custom definition for the word" in {
      val mockRepo = buildWordsRepo(getResult = Some(mockWord))
      val result = buildController(mockRepo).get(mockWord.word)(FakeRequest())
      status(result) mustBe OK
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("#custom-definition-panel .panel-body").text mustBe mockWord.definitions.mkString(", ")
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
