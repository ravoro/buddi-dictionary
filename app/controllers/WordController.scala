package controllers

import javax.inject._

import forms.WordForm.{form => wordForm}
import models.Word
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.WordsRepository
import sources.{WiktionarySource, YandexDictionarySource, YandexTranslateSource}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

@Singleton
class WordController @Inject()(val messagesApi: MessagesApi,
                               val wiktionarySource: WiktionarySource,
                               val yandexTranslateSource: YandexTranslateSource,
                               val yandexDictionarySource: YandexDictionarySource,
                               val wordsRepo: WordsRepository) extends Controller with I18nSupport {

  def editForm(word: String) = Action.async { implicit request =>
    wordsRepo.get(word).map { wordOpt =>
      val form = wordOpt.fold(wordForm(word))(wordForm(word).fill)
      Ok(views.html.wordForm(word, form))
    }
  }

  def edit(word: String) = Action.async { implicit request =>
    wordForm(word).bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.wordForm(word, formWithErrors)))
      },
      submission => {
        val newWord = Word(None, submission.word, submission.lang, submission.definitions)
        wordsRepo.upsert(newWord).map {
          case Success(_) => {
            Redirect(routes.WordController.get(submission.word))
              .flashing("message" -> s"""Successfully updated definition of "$word".""")
          }
          case Failure(e) => {
            println(s"Failed to save word: ${e.getMessage}")
            val formWithError = wordForm(word).withGlobalError("Error occurred while saving, please try again.")
            InternalServerError(views.html.wordForm(word, formWithError))
          }
        }
      }
    )
  }

  def get(word: String) = Action.async { implicit request =>
    for {
      customOpt <- wordsRepo.get(word)
      wikiOpt <- wiktionarySource.get(word)
      yandexTransOpt <- yandexTranslateSource.get(word)
      yandexDictOpt <- yandexDictionarySource.get(word)
    } yield Ok(views.html.word(word, customOpt, wikiOpt, yandexTransOpt, yandexDictOpt))
  }

  def getAll() = Action.async {
    wordsRepo.getAll().map { words =>
      Ok(views.html.wordAll(words))
    }
  }
}
