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

  def editForm(word: String, lang: String) = Action.async { implicit request =>
    wordsRepo.get(word, lang).map { wordOpt =>
      val form = wordOpt.fold(wordForm(word, lang))(wordForm(word, lang).fill)
      Ok(views.html.wordForm(word, lang, form))
    }
  }

  def edit(word: String, lang: String) = Action.async { implicit request =>
    wordForm(word, lang).bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.wordForm(word, lang, formWithErrors)))
      },
      submission => {
        val newWord = Word(None, submission.word, submission.lang, submission.definitions)
        wordsRepo.upsert(newWord).map {
          case Success(_) => {
            Redirect(routes.WordController.get(word, lang))
              .flashing("message" -> s"""Successfully updated definition of "$word".""")
          }
          case Failure(e) => {
            println(s"Failed to save word: ${e.getMessage}")
            val formWithError = wordForm(word, lang).withGlobalError("Error occurred while saving, please try again.")
            InternalServerError(views.html.wordForm(word, lang, formWithError))
          }
        }
      }
    )
  }

  def get(word: String, lang: String) = Action.async { implicit request =>
    if (!forms.WordForm.validLanguages.contains(lang)) {
      Future.successful(BadRequest)
    } else {
      for {
        customOpt <- wordsRepo.get(word, lang)
        wikiOpt <- wiktionarySource.get(word)
        yandexTransOpt <- yandexTranslateSource.get(word)
        yandexDictOpt <- yandexDictionarySource.get(word)
      } yield Ok(views.html.word(word, lang, customOpt, wikiOpt, yandexTransOpt, yandexDictOpt))
    }
  }

  def getAll() = Action.async {
    wordsRepo.getAll().map { words =>
      Ok(views.html.wordAll(words))
    }
  }
}
