package forms

import play.api.data.Form
import play.api.data.Forms._

case class SearchBox(query: String)

object SearchBoxForm {
  val form = Form(
    mapping(
      "query" -> text.transform[String](_.trim, _.toString)
    )(SearchBox.apply)(SearchBox.unapply)
  )
}
