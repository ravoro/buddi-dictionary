package controllers

import javax.inject._

import forms.WordForm.{form => wordForm}
import models.Word
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.WordRepository
import sources.WiktionarySource

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

@Singleton
class WordController @Inject()(val messagesApi: MessagesApi,
                               val wikiSource: WiktionarySource,
                               val wordsRepo: WordRepository) extends Controller with I18nSupport {

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
        wordsRepo.get(word).flatMap { wordOpt =>
          val idOpt = wordOpt.flatMap(_.id)
          val newRecord = Word(idOpt, submission.word, submission.definition)
          wordsRepo.upsert(newRecord).map {
            case Success(_) => {
              Redirect(routes.WordController.get(submission.word))
                .flashing("message" -> s"""Successfully updated definition of "$word".""")
            }
            case Failure(e) => {
              val formWithError = wordForm(word).withGlobalError("Error occurred while saving, please try again.")
              InternalServerError(views.html.wordForm(word, formWithError))
            }
          }
        }
      }
    )
  }

  def get(word: String) = Action.async { implicit request =>
    for {
      customOpt <- wordsRepo.get(word)
      wikiOpt <- wikiSource.get(word)
    } yield Ok(views.html.word(word, customOpt, wikiOpt))
  }

  def getAll() = Action.async {
    wordsRepo.getAll().map { words =>
      Ok(views.html.wordAll(words))
    }
  }
}
