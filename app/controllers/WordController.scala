package controllers

import javax.inject._

import forms.WordForm.addForm
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.WordRepository
import repositories.WordRepository.DuplicateRecordException

import scala.util.{Failure, Success}

@Singleton
class WordController @Inject()(val messagesApi: MessagesApi,
                               val wordsRepo: WordRepository) extends Controller with I18nSupport {

  def addGet() = Action { implicit request =>
    Ok(views.html.wordAdd(addForm))
  }

  def addPost() = Action { implicit request =>
    addForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.wordAdd(formWithErrors))
      },
      submission => {
        wordsRepo.add(submission.word, submission.definition) match {
          case Success(_) => Redirect(routes.WordController.get(submission.word))
          case Failure(e: DuplicateRecordException) => BadRequest(views.html.wordAdd(addForm)).flashing("msggg" -> "wordalreadyexists")
          case Failure(e) => throw e
        }
      }
    )
  }

  def get(word: String) = Action {
    wordsRepo.get(word) match {
      case Some(w) => Ok(views.html.word(w))
      case _ => NotFound
    }
  }

  def getAll() = Action {
    Ok(views.html.wordAll(wordsRepo.getAll()))
  }
}
