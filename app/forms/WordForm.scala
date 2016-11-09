package forms

import models.Word
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}


object WordForm {
  val addForm = Form(
    mapping(
      "word" -> nonEmptyText,
      "definition" -> nonEmptyText
    )(Word.apply)(Word.unapply)
  )
}
