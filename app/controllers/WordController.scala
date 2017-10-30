package controllers

import javax.inject._

import forms.WordForm.{form => wordForm}
import models.Word
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.WordRepository
import sources.{WiktionarySource, YandexDictionarySource, YandexTranslateSource}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

@Singleton
class WordController @Inject()(val messagesApi: MessagesApi,
                               val wiktionarySource: WiktionarySource,
                               val yandexTranslateSource: YandexTranslateSource,
                               val yandexDictionarySource: YandexDictionarySource,
                               val wordRepo: WordRepository) extends Controller with I18nSupport {

  def editForm(word: String) = Action.async { implicit request =>
    wordRepo.get(word).map { wordOpt =>
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
        val newWord = Word(None, submission.word, submission.definitions)
        wordRepo.upsert(newWord).map {
          case Success(_) => {
            Redirect(routes.WordController.get(submission.word))
              .flashing("message" -> s"""Successfully updated definition of "$word".""")
          }
          case Failure(e) => {
            // TODO: add logging - s"Failed to save word: ${e.getMessage}"
            val formWithError = wordForm(word).withGlobalError("Error occurred while saving, please try again.")
            InternalServerError(views.html.wordForm(word, formWithError))
          }
        }
      }
    )
  }

  def get(word: String) = Action.async { implicit request =>
    for {
      customOpt <- wordRepo.get(word)
      wikiOpt <- wiktionarySource.get(word)
      yandexTransOpt <- yandexTranslateSource.get(word)
      yandexDictOpt <- yandexDictionarySource.get(word)
    } yield Ok(views.html.word(word, customOpt, wikiOpt, yandexTransOpt, yandexDictOpt))
  }

  def getAll() = Action.async { implicit request =>
    wordRepo.getAll().map { words =>
      Ok(views.html.wordAll(words))
    }
  }
}
