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
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def word = column[String]("word")

    def definition = column[String]("definition")

    def * = (id.?, word, definition) <> (Word.tupled, Word.unapply)
  }

  private val words = TableQuery[WordsTable]

  def get(word: String): Future[Option[Word]] = db.run {
    words.filter(_.word === word).result.headOption
  }

  def getAll(): Future[Seq[Word]] = db.run {
    words.result
  }

  def upsert(word: Word): Future[Try[Unit]] = db.run {
    words.insertOrUpdate(word).map { rows =>
      if (rows > 0) Success(Unit)
      else Failure(new Exception(s"Failed to upsert '$word'"))
    }
  }
}
