package repositories

import org.mockito.ArgumentMatchers._
import models.Word
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class WordRepositorySpec extends PlaySpec with GuiceOneAppPerTest with FutureAwaits with DefaultAwaitTimeout with MockitoSugar {

  def buildRepo(words: Seq[Word] = Seq())(implicit app: Application): WordRepository = {
    val repo = app.injector.instanceOf(classOf[WordRepository])
    await(Future.sequence(words.map(repo.upsert)))
    repo
  }

  private def ignoreID(word: Option[Word]): Word = word.get.copy(id = None)

  val mockWords = List(
    Word(None, "bye", Seq("goodbye", "see you later")),
    Word(None, "hello", Seq("hey", "hi", "a form of greeting"))
  )


  "get" must {
    "return None if the given word does not exist" in {
      val result = await(buildRepo(mockWords).get("random"))
      result mustBe None
    }

    "return a Word record with no definitions, if the word exists but has no values in the definitions table" in {
      val mockWord = Word(None, "awesome", Seq())
      val mockData = mockWord :: mockWords
      val result = await(buildRepo(mockData).get("awesome"))
      result.get.word mustBe mockWord.word
      result.get.definitions mustBe Seq()
    }

    "return a Word record with a definition, if there is one definition for it in the definitions table" in {
      val mockWord = Word(None, "awesome", Seq("great"))
      val mockData = mockWord :: mockWords
      val result = await(buildRepo(mockData).get("awesome"))
      result.get.word mustBe mockWord.word
      result.get.definitions mustBe mockWord.definitions
    }

    "return a Word record with multiple definitions, if the word has several definitions in the definitions table" in {
      val mockWord = Word(None, "awesome", Seq("great", "cool", "phenomenal"))
      val mockData = mockWord :: mockWords
      val result = await(buildRepo(mockData).get("awesome"))
      result.get.word mustBe mockWord.word
      result.get.definitions mustBe mockWord.definitions
    }
  }


  "getAll" must {
    "return an empty list if there are no words" in {
      val result = await(buildRepo().getAll())
      result mustBe List()
    }

    "return a list of all user defined words in the db" in {
      val result = await(buildRepo(mockWords).getAll())
      val resultWithoutIds = result.map(_.copy(id = None))
      resultWithoutIds mustBe mockWords
    }

    "ensure returned list is sorted aphabetically" in {
      val wordsUnsorted = mockWords ++ List(
        Word(None, "zebra", Seq("an animal", "striped horse")),
        Word(None, "apple", Seq("not a pear")),
        Word(None, "human", Seq("person", "not a zebra", "thinks its smart"))
      )
      val result = await(buildRepo(wordsUnsorted).getAll())
      result(0).word mustBe "apple"
      result(1).word mustBe "bye"
      result(2).word mustBe "hello"
      result(3).word mustBe "human"
      result(4).word mustBe "zebra"
    }
  }


  "upsert" must {
    val mockWord = Word(None, "awesome", Seq("great", "cool", "phenomenal"))

    "insert a new word with given definitions, when the word does not exist in db" in {
      val repo = buildRepo(mockWords)

      val oldRecord = await(repo.get(mockWord.word))
      val result = await(repo.upsert(mockWord))
      val newRecord = await(repo.get(mockWord.word))

      result.isSuccess mustBe true
      oldRecord mustBe None
      ignoreID(newRecord) mustBe mockWord
    }

    "update the word by inserting any new definitions" in {
      val newWord = mockWord.copy(definitions = mockWord.definitions ++ Seq("yay", "legit"))
      val oldData = mockWord :: mockWords
      val repo = buildRepo(oldData)

      val oldRecord = await(repo.get(mockWord.word))
      val result = await(repo.upsert(newWord))
      val newRecord = await(repo.get(mockWord.word))

      result.isSuccess mustBe true
      ignoreID(oldRecord) mustBe mockWord
      ignoreID(newRecord) mustBe newWord
    }

    "update the word by removing any definitions that are not a part of the latest definitions list" in {
      val newWord = mockWord.copy(definitions = mockWord.definitions diff Seq("cool", "phenomenal"))
      val oldData = mockWord :: mockWords
      val repo = buildRepo(oldData)

      val oldRecord = await(repo.get(mockWord.word))
      val result = await(repo.upsert(newWord))
      val newRecord = await(repo.get(mockWord.word))

      result.isSuccess mustBe true
      ignoreID(oldRecord) mustBe mockWord
      ignoreID(newRecord) mustBe newWord
    }

    "update the word, by inserting new and removing old definitions, to match the latest definitions list" in {
      val newWord = mockWord.copy(definitions = (mockWord.definitions diff Seq("cool", "phenomenal")) ++ Seq("yay", "legit"))
      val oldData = mockWord :: mockWords
      val repo = buildRepo(oldData)

      val oldRecord = await(repo.get(mockWord.word))
      val result = await(repo.upsert(newWord))
      val newRecord = await(repo.get(mockWord.word))

      result.isSuccess mustBe true
      ignoreID(oldRecord) mustBe mockWord
      ignoreID(newRecord) mustBe newWord
    }

    "handle any exceptions raised while inserting and return a Failure" in {
      val mockRecord = Some(mockWord.copy(id = Some(123)))

      val repo = mock[WordRepository]
      when(repo.upsert(any())).thenCallRealMethod()
      when(repo.get(any())).thenReturn(Future.successful(mockRecord))
      when(repo.insertDefinitionBatch(any(), any())).thenReturn(Future.successful(Failure(new Exception)))
      when(repo.deleteDefinitionBatch(any(), any())).thenReturn(Future.successful(Success(Unit)))

      val result = await(repo.upsert(mockWord))

      result.isFailure mustBe true
    }

    "handle any exceptions raised while deleting and return a Failure" in {
      val mockRecord = Some(mockWord.copy(id = Some(123)))

      val repo = mock[WordRepository]
      when(repo.upsert(any())).thenCallRealMethod()
      when(repo.get(any())).thenReturn(Future.successful(mockRecord))
      when(repo.insertDefinitionBatch(any(), any())).thenReturn(Future.successful(Success(Unit)))
      when(repo.deleteDefinitionBatch(any(), any())).thenReturn(Future.successful(Failure(new Exception)))
      val result = await(repo.upsert(mockWord))

      result.isFailure mustBe true
    }
  }
}
