package forms

import models.Word
import play.api.data.Form
import play.api.data.Forms._


object WordForm {
  val validLanguages = List("en", "ru")

  def form(word: String): Form[Word] = Form(
    mapping(
      "id" -> optional(longNumber),
      "word" -> ignored(word),
      "lang" -> nonEmptyText.verifying(
        s"Language needs to be one of (${validLanguages.mkString(", ")})",
        s => validLanguages.contains(s)),
      "definitions" -> seqTextNewLineSeparated
    )(Word.apply)(Word.unapply)
  )
}
