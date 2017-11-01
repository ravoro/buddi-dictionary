package repositories

import models.db.DefinitionRecord
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait DefinitionComponent extends WordComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import driver.api._

  class Definitions(tag: Tag) extends Table[DefinitionRecord](tag, "definitions") {
    def wid = column[Long]("wid")

    def definition = column[String]("definition")

    def * = (wid, definition) <> (DefinitionRecord.tupled, DefinitionRecord.unapply)

    def word = foreignKey("fk_words", wid, words)(_.id)
  }

  val definitions = TableQuery[Definitions]

  def insertDefinitionBatch(wordID: Long, defs: Set[String]): Future[Try[Any]] = {
    if (defs.isEmpty) Future.successful(Success(Unit))
    else {
      val defsRecords = defs.map(DefinitionRecord(wordID, _))
      val query = definitions ++= defsRecords
      db.run(query).map {
        case Some(x) if x > 0 => Success(Unit)
      }.recover {
        case e => Failure(new Exception(
          s"Failed to insert definitions ($defs) for word id=$wordID.\nError details: ${e.getMessage}"))
      }
    }
  }

  def deleteDefinitionBatch(wordID: Long, defs: Set[String]): Future[Try[Any]] = {
    if (defs.isEmpty) Future.successful(Success(Unit))
    else {
      val query = definitions.filter(record => record.wid === wordID && record.definition.inSet(defs))
      db.run(query.delete).map {
        case count if count > 0 => Success(Unit)
      }.recover {
        case e => Failure(new Exception(
          s"Failed to delete definitions ($defs) for word id=$wordID.\nError details: ${e.getMessage}"))
      }
    }
  }
}
