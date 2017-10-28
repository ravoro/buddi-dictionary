package controllers

import javax.inject._

import forms.SearchBoxForm.form
import play.api.mvc.{Action, Controller}

@Singleton
class SearchBoxController @Inject()() extends Controller {
  def search = Action { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => BadRequest,
      submission => Redirect(routes.WordController.get(submission.query))
    )
  }
}
