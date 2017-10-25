package forms

import models.Word
import play.api.data.Form
import play.api.data.Forms._


object WordForm {
  val validLanguages = List("en", "ru")

  def form(word: String, lang: String): Form[Word] = Form(
    mapping(
      "id" -> optional(longNumber),
      "word" -> ignored(word),
      "lang" -> ignored(lang),
      "definitions" -> definitions
    )(Word.apply)(Word.unapply)
  )
}
