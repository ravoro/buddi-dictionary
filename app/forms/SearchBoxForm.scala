package forms

import play.api.data.Form
import play.api.data.Forms._

case class SearchBox(query: String)

object SearchBoxForm {
  val form = Form(
    mapping(
      "query" -> text
    )(SearchBox.apply)(SearchBox.unapply)
  )
}
