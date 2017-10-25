package repositories

import models.db.WordRecord
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait WordsComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import driver.api._

  class WordsTable(tag: Tag) extends Table[WordRecord](tag, "words") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def word = column[String]("word")

    def lang = column[String]("lang")

    def * = (id.?, word, lang) <> (WordRecord.tupled, WordRecord.unapply)
  }

  val words = TableQuery[WordsTable]

  def insertWordRecord(word: String, lang: String): Future[Long] = {
    val query = (words returning words.map(_.id)) += WordRecord(None, word, lang)
    db.run(query)
  }
}
