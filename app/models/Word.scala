package models

case class Word(id: Option[Long], word: String, lang: String, definitions: Seq[String])

object db {

  case class WordRecord(id: Option[Long], word: String, lang: String)

  case class DefinitionRecord(wid: Long, definition: String)

}
