package models

case class Word(id: Option[Long], word: String, lang: String, definitions: List[WordDefinition]) {
  val defsToMap: Map[String, List[String]] = {
    val mapByLang = definitions.groupBy(_.lang)
    val mapLangToDefs = mapByLang.mapValues(_.map(_.definition))
    mapLangToDefs
  }
}

case class WordDefinition(definition: String, lang: String)

object db {

  case class WordRecord(id: Option[Long], word: String, lang: String)

  case class DefinitionRecord(wid: Long, definition: String, lang: String)

}
