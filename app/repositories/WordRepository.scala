package repositories

import javax.inject._

import models.Word
import WordRepository.DuplicateRecordException

import scala.util.{Failure, Try}

@Singleton
class WordRepository {
  val db = collection.mutable.Map[String, String]()

  def add(word: String, definition: String): Try[Unit] = get(word) match {
    case Some(_) => Failure(new DuplicateRecordException(s"""DB integrity error: The word "$word" already exists in the db."""))
    case _ => Try(db.update(word, definition))
  }

  def get(word: String): Option[Word] = db.get(word).map(definition => Word(word, definition))
}

object WordRepository {
  case class DuplicateRecordException(message: String) extends Exception(message)
}