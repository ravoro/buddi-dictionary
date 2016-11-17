package repositories

import javax.inject._

import models.Word

import scala.util.Try

@Singleton
class WordRepository {
  val db = collection.mutable.Map[String, String]()

  def get(word: String): Option[Word] = db.get(word).map(definition => Word(word, definition))

  def getAll(): List[Word] = db.toList.map { case (w, d) => Word(w, d) }

  def upsert(word: String, definition: String): Try[Unit] = Try(db.update(word, definition))
}
