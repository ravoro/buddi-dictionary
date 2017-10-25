package controllers

import javax.inject._

import forms.SearchBoxForm.form
import play.api.mvc.{Action, Controller}

@Singleton
class SearchBoxController @Inject()() extends Controller {
  def search = Action { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => BadRequest,
      submission => {
        val path =
          submission.query.fold {
            routes.WordController.getAll
          } { q =>
            routes.WordController.get(q.definition, q.lang)
          }
        Redirect(path)
      }
    )
  }
}
