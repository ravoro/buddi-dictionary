package forms

import models.Word
import play.api.data.Form
import play.api.data.Forms._


object WordForm {
  def form(word: String): Form[Word] = Form(
    mapping(
      "word" -> ignored(word),
      "definition" -> text
    )(Word.apply)(Word.unapply)
  )
}
