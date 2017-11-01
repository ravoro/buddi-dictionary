package repositories

import models.db.WordRecord
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.Future

trait WordComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import driver.api._

  class Words(tag: Tag) extends Table[WordRecord](tag, "words") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def word = column[String]("word")

    def * = (id.?, word) <> (WordRecord.tupled, WordRecord.unapply)
  }

  val words = TableQuery[Words]

  def insertWord(word: String): Future[Long] = {
    val query = (words returning words.map(_.id)) += WordRecord(None, word)
    db.run(query)
  }
}
