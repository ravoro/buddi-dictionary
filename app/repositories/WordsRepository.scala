package repositories

import javax.inject._

import models.db.{DefinitionRecord, WordRecord}
import models.{Word, WordDefinition}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class WordsRepository @Inject()(val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] with WordsComponent with DefinitionsComponent {

  import driver.api._

  private def rowsToMap(rows: List[(WordRecord, Option[DefinitionRecord])]): Map[Long, Word] = {
    val mapRecordsByWord = rows.groupBy(_._1)
    val mapDefsByWord = mapRecordsByWord.map { case (word, records) => (word, records.flatMap(_._2)) }
    val mapWordsById = mapDefsByWord.map {
      case (w, d) => (w.id.get, Word(w.id, w.word, w.lang, d.map(rec => WordDefinition(rec.definition, rec.lang))))
    }
    mapWordsById
  }

  def get(word: String, lang: String): Future[Option[Word]] = {
    val query = for {
      (w, d) <- words joinLeft definitions on (_.id === _.wid)
      if w.word === word && w.lang === lang
    } yield (w, d)

    def findWordByNameLang(records: Map[Long, Word], word: String): Option[Word] =
      records.find { case (_, w) => w.word == word && w.lang == lang }.map(_._2)

    for {
      rows <- db.run(query.result)
    } yield findWordByNameLang(rowsToMap(rows.toList), word)
  }

  def getAll(): Future[Seq[Word]] = {
    val query = for {
      (w, d) <- words joinLeft definitions on (_.id === _.wid)
    } yield (w, d)

    for {
      rows <- db.run(query.result)
    } yield rowsToMap(rows.toList).values.toSeq
  }

  def upsert(word: Word): Future[Try[Unit]] = {
    /*
     * Attempts to fetch and return an existing Word.
     * If the word does not exist, creates a new WordRecord and returns the resulting Word with no definitions.
     */
    def getOrInitialize(word: String, lang: String): Future[Word] = {
      get(word, lang).flatMap { wordOpt =>
        wordOpt.fold {
          insertWordRecord(word, lang).map { id =>
            Word(Some(id), word, lang, List())
          }
        }(Future.successful(_))
      }
    }

    def splitDefinitions(oldWord: Word, newWord: Word): (Set[WordDefinition], Set[WordDefinition], Set[WordDefinition]) = {
      val oldDefs = oldWord.definitions.toSet
      val newDefs = word.definitions.toSet
      val toRemain = oldDefs intersect newDefs
      val toDelete = oldDefs diff toRemain
      val toInsert = newDefs diff toRemain
      println(s"split definitions: $oldDefs...$newDefs...$toRemain...$toDelete...$toInsert")
      (toRemain, toDelete, toInsert)
    }

    // TODO: needs to be a transaction
    // TODO: word needs to be deleted if newWord.defs.isEmpty
    for {
      oldWord <- getOrInitialize(word.word, word.lang)
      wordID = oldWord.id.get
      (toRemain, toDelete, toInsert) = splitDefinitions(oldWord, word)
      deleteResult <- deleteDefinitionRecordBatch(wordID, toDelete)
      insertResult <- insertDefinitionRecordBatch(wordID, toInsert)
    } yield {
      if (insertResult.isSuccess && deleteResult.isSuccess) {
        Success(Unit)
      } else {
        def resultStatus(result: Try[Unit]) = result match {
          case Success(_) => "Success"
          case Failure(e) => s"Failure: ${e.getMessage}}"
        }
        Failure(new Exception(
          s"""Failed to upsert word=$word.
              |insert=[${resultStatus(insertResult)}].
              |delete=[${resultStatus(deleteResult)}].""".stripMargin.replaceAll("\n", " ")))
      }
    }
  }
}
