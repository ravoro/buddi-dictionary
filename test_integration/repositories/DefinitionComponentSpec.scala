package repositories

import models.Word
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DefinitionComponentSpec extends PlaySpec with GuiceOneAppPerTest with FutureAwaits with DefaultAwaitTimeout {

  def buildRepo(words: Seq[Word] = Seq())(implicit app: Application): WordRepository = {
    val repo = app.injector.instanceOf(classOf[WordRepository])
    await(Future.sequence(words.map(repo.upsert)))
    repo
  }

  val oldDefs = Seq("hey", "hi", "a form of greeting")
  val oldWord = Word(None, "hello", oldDefs)
  val oldWords = List(
    Word(None, "bye", Seq("goodbye", "see you later")),
    oldWord
  )

  "insertDefinitionBatch" must {
    "make no changes if the given value is empty" in {
      val newDefs = Set.empty[String]
      val repo = buildRepo(oldWords)

      val oldRecord = await(repo.get(oldWord.word))
      val result = await(repo.insertDefinitionBatch(oldRecord.get.id.get, newDefs))
      val newRecord = await(repo.get(oldWord.word))

      result.isSuccess mustBe true
      newRecord.get.definitions must not be newDefs
      newRecord.get.definitions mustBe oldDefs
    }

    "append any new definitions" in {
      val newDefs = Set("what's up", "how are you")
      val repo = buildRepo(oldWords)

      val oldRecord = await(repo.get(oldWord.word))
      val result = await(repo.insertDefinitionBatch(oldRecord.get.id.get, newDefs))
      val newRecord = await(repo.get(oldWord.word))

      result.isSuccess mustBe true
      newRecord.get.definitions must not be oldDefs
      newRecord.get.definitions must not be newDefs
      newRecord.get.definitions mustBe oldDefs ++ newDefs
    }

    "append any duplicate definitions" in {
      val newDefs = Set("hey", "a form of greeting")
      val repo = buildRepo(oldWords)

      val oldRecord = await(repo.get(oldWord.word))
      val result = await(repo.insertDefinitionBatch(oldRecord.get.id.get, newDefs))
      val newRecord = await(repo.get(oldWord.word))

      result.isSuccess mustBe true
      newRecord.get.definitions must not be oldDefs
      newRecord.get.definitions must not be newDefs
      newRecord.get.definitions mustBe oldDefs ++ newDefs
      newRecord.get.definitions.count(_ == "hey") mustBe 2
      newRecord.get.definitions.count(_ == "hi") mustBe 1
      newRecord.get.definitions.count(_ == "a form of greeting") mustBe 2
    }

    "handle any exception and return a Failure" in {
      val newDefs = Set("test")
      val invalidID = 1234
      val repo = buildRepo(oldWords)

      val result = await(repo.insertDefinitionBatch(invalidID, newDefs))

      result.isFailure mustBe true
    }
  }

  "deleteDefinitionBatch" must {
    "make no changes if the given value is empty" in {
      val deleteDefs = Set.empty[String]
      val repo = buildRepo(oldWords)

      val oldRecord = await(repo.get(oldWord.word))
      val result = await(repo.deleteDefinitionBatch(oldRecord.get.id.get, deleteDefs))
      val newRecord = await(repo.get(oldWord.word))

      result.isSuccess mustBe true
      newRecord.get.definitions must not be deleteDefs
      newRecord.get.definitions mustBe oldDefs
    }

    "ignore any non-existing definitions" in {
      val deleteDefs = Set("hey", "what's up", "how are you")
      val repo = buildRepo(oldWords)

      val oldRecord = await(repo.get(oldWord.word))
      val result = await(repo.deleteDefinitionBatch(oldRecord.get.id.get, deleteDefs))
      val newRecord = await(repo.get(oldWord.word))

      result.isSuccess mustBe true
      newRecord.get.definitions must not be deleteDefs
      newRecord.get.definitions mustBe oldDefs.filter(_ != "hey")
    }

    "delete any provided definitions" in {
      val deleteDefs = Set("hey", "a form of greeting")
      val repo = buildRepo(oldWords)

      val oldRecord = await(repo.get(oldWord.word))
      val result = await(repo.deleteDefinitionBatch(oldRecord.get.id.get, deleteDefs))
      val newRecord = await(repo.get(oldWord.word))

      result.isSuccess mustBe true
      newRecord.get.definitions must not be oldDefs
      newRecord.get.definitions must not be deleteDefs
      newRecord.get.definitions mustBe Seq("hi")
    }

    "handle any exception and return a Failure" in {
      val deleteDefs = Set("hey")
      val invalidID = 1234
      val repo = buildRepo(oldWords)

      val result = await(repo.deleteDefinitionBatch(invalidID, deleteDefs))

      result.isFailure mustBe true
    }
  }
}
