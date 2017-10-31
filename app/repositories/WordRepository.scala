package repositories

import javax.inject._

import models.Word
import models.db.{DefinitionRecord, WordRecord}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class WordRepository @Inject()(val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] with WordsComponent with DefinitionsComponent {

  import driver.api._

  private def rowsToMap(rows: Seq[(WordRecord, Option[DefinitionRecord])]): Map[Long, Word] = {
    val mapRecordsByWord = rows.groupBy(_._1)
    val mapDefsByWord = mapRecordsByWord.map { case (word, records) => (word, records.flatMap(_._2)) }
    val mapWordsById = mapDefsByWord.map { case (w, d) => (w.id.get, Word(w.id, w.word, d.map(_.definition))) }
    mapWordsById
  }

  def get(word: String): Future[Option[Word]] = {
    val query = for {
      (w, d) <- words joinLeft definitions on (_.id === _.wid)
      if w.word === word
    } yield (w, d)

    def findWordByName(records: Map[Long, Word], word: String): Option[Word] =
      records.find(_._2.word == word).map(_._2)

    for {
      rows <- db.run(query.result)
    } yield findWordByName(rowsToMap(rows), word)
  }

  def getAll(): Future[List[Word]] = {
    val query = for {
      (w, d) <- words joinLeft definitions on (_.id === _.wid)
    } yield (w, d)

    for {
      rows <- db.run(query.result)
    } yield rowsToMap(rows).values.toList.sortBy(_.word)
  }

  def upsert(word: Word): Future[Try[Unit]] = {
    /*
     * Attempts to fetch and return an existing Word.
     * If the word does not exist, creates a new WordRecord and returns the resulting Word with no definitions.
     */
    def getOrInitialize(word: String): Future[Word] = {
      get(word).flatMap { wordOpt =>
        wordOpt.fold {
          insertWordRecord(word).map { id =>
            Word(Some(id), word, Seq())
          }
        }(Future.successful(_))
      }
    }

    def splitDefinitions(oldWord: Word, newWord: Word): (Set[String], Set[String], Set[String]) = {
      val oldDefs = oldWord.definitions.toSet
      val newDefs = word.definitions.toSet
      val toRemain = oldDefs intersect newDefs
      val toDelete = oldDefs diff toRemain
      val toInsert = newDefs diff toRemain
      println(s"split definitions: $oldDefs...$newDefs...$toRemain...$toDelete...$toInsert")
      (toRemain, toDelete, toInsert)
    }

    // TODO: needs to be a transaction
    for {
      oldWord <- getOrInitialize(word.word)
      wordID = oldWord.id.get
      (toRemain, toDelete, toInsert) = splitDefinitions(oldWord, word)
      insertResult <- insertDefinitionRecordBatch(wordID, toInsert)
      deleteResult <- deleteDefinitionRecordBatch(wordID, toDelete)
    } yield {
      if (insertResult.isSuccess && deleteResult.isSuccess) {
        Success(Unit)
      } else {
        def resultStatus(result: Try[Unit]) = result match {
          case Success(_) => "Success"
          case Failure(e) => s"Failure: ${e.getMessage}}"
        }
        Failure(new Exception(s"Failed to upsert word=$word. insert=[${resultStatus(insertResult)}]. delete=[${resultStatus(deleteResult)}]"))
      }
    }
  }
}
