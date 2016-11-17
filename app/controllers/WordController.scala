package controllers

import javax.inject._

import forms.WordForm.{form => wordForm}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.WordRepository

import scala.util.{Failure, Success}

@Singleton
class WordController @Inject()(val messagesApi: MessagesApi,
                               val wordsRepo: WordRepository) extends Controller with I18nSupport {

  def editForm(word: String) = Action { implicit request =>
    val form = wordsRepo.get(word).fold(wordForm(word))(wordForm(word).fill)
    Ok(views.html.wordForm(word, form))
  }

  def edit(word: String) = Action { implicit request =>
    wordForm(word).bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.wordForm(word, formWithErrors))
      },
      submission => {
        wordsRepo.upsert(submission.word, submission.definition) match {
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
    )
  }

  def get(word: String) = Action { implicit request =>
    val wordOpt = wordsRepo.get(word)
    Ok(views.html.word(word, wordOpt))
  }

  def getAll() = Action {
    Ok(views.html.wordAll(wordsRepo.getAll()))
  }
}
