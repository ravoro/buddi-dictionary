package repositories

import javax.inject._

import models.Word
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class WordRepository @Inject()(dbConfigProvider: DatabaseConfigProvider) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val db = dbConfig.db

  import dbConfig.driver.api._

  private class WordsTable(tag: Tag) extends Table[Word](tag, "words") {
    def word = column[String]("word")

    def definition = column[String]("definition")

    def * = (word, definition) <> (Word.tupled, Word.unapply)
  }

  private val words = TableQuery[WordsTable]

  def get(word: String): Future[Option[Word]] = db.run {
    words.filter(_.word === word).result.headOption
  }

  def getAll(): Future[Seq[Word]] = db.run {
    words.result
  }

  def upsert(word: String, definition: String): Future[Try[Unit]] = db.run {
    words.insertOrUpdate(Word(word, definition)).map { rows =>
      if (rows > 0) Success(Unit)
      else Failure(new Exception(s"Failed to upsert '$word'"))
    }
  }
}
