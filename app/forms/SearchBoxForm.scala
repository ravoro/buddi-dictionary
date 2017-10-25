package forms

import models.WordDefinition
import play.api.data.Form
import play.api.data.Forms._

case class SearchBox(query: Option[WordDefinition])

object SearchBoxForm {
  val form = Form(
    mapping(
      "query" -> definitions.transform[Option[WordDefinition]](
        _.headOption,
        _.fold(List.empty[WordDefinition])(List(_))
      )
    )(SearchBox.apply)(SearchBox.unapply)
  )
}
