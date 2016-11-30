package repositories

import models.WordDefinition
import models.db.DefinitionRecord
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait DefinitionsComponent extends WordsComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import driver.api._

  class DefinitionsTable(tag: Tag) extends Table[DefinitionRecord](tag, "definitions") {
    def wid = column[Long]("wid")

    def definition = column[String]("definition")

    def lang = column[String]("lang")

    def * = (wid, definition, lang) <> (DefinitionRecord.tupled, DefinitionRecord.unapply)

    def word = foreignKey("fk_words", wid, words)(_.id)
  }

  val definitions = TableQuery[DefinitionsTable]

  def insertDefinitionRecordBatch(wordID: Long, defs: Set[WordDefinition]): Future[Try[Unit]] = {
    if (defs.isEmpty) Future.successful(Success(Unit))
    else {
      val defsRecords = defs.map(d => DefinitionRecord(wordID, d.definition, d.lang))
      val query = definitions ++= defsRecords
      db.run(query).map {
        case Some(x) if x > 0 => Success(Unit)
        case _ => Failure(new Exception(s"Failed to insert definitions ($defs) for word id=$wordID"))
      }
    }
  }

  def deleteDefinitionRecordBatch(wordID: Long, defs: Set[WordDefinition]): Future[Try[Unit]] = {
    if (defs.isEmpty) Future.successful(Success(Unit))
    else {
      // !!!
      // TODO: currently deleting based on wid+definition (not considering lang!). Assuming all definitions unique (i.e. no same word in diff langs)
      // !!!
      val query = definitions.filter(rec => rec.wid === wordID && rec.definition.inSet(defs.map(_.definition)))
      db.run(query.delete).map {
        case count if count > 0 => Success(Unit)
        case _ => Failure(new Exception(s"Failed to delete definitions ($defs) for word id=$wordID"))
      }
    }
  }
}
