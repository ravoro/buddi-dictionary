package repositories

import javax.inject._

import models.Word

@Singleton
class WordRepository {
  var db = Map[String, String](
    "apple" -> "sort of fruit",
    "ball" -> "spherical object"
  )

  def get(word: String): Option[Word] = db.get(word).map(definition => Word(word, definition))
}
