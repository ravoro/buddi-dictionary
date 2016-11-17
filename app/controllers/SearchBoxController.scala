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
          if (submission.query.isEmpty) routes.WordController.getAll
          else routes.WordController.get(submission.query)
        Redirect(path)
      }
    )
  }
}
