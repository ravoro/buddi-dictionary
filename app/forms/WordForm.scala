package forms

import models.Word
import play.api.data.Form
import play.api.data.Forms._


object WordForm {
  def form(word: String): Form[Word] = Form(
    mapping(
      "id" -> optional(longNumber),
      "word" -> ignored(word),
      "definitions" -> seqTextNewLineSeparated
    )(Word.apply)(Word.unapply)
  )
}
