package repositories

import models.Word
import org.scalatestplus.play.PlaySpec

class WordRepositorySpec extends PlaySpec {
  val mockDb = Map(
    "apple" -> "type of fruit",
    "ball" -> "item for playing")

  def buildRepository(records: List[(String, String)] = mockDb.toList) = {
    val repo = new WordRepository()
    records.foreach { case (word, definition) => repo.upsert(word, definition) }
    repo
  }

  "get" must {
    "return the record for the provided word" in {
      val result = buildRepository().get("apple")
      val expected = Some(Word("apple", mockDb("apple")))
      result mustBe expected
    }

    "return None when a record does not exist for the provided word" in {
      val result = buildRepository().get("randomword")
      val expected = None
      result mustBe expected
    }
  }

  "getAll" must {
    "return all records in db" in {
      val result = buildRepository().getAll()
      val expected = mockDb.toList.map { case (w, d) => Word(w, d) }
      result mustBe expected
    }

    "return an empty list when there are no records in the db" in {
      val result = buildRepository(Nil).getAll()
      val expected = Nil
      result mustBe expected
    }
  }

  "upsert" must {
    "return Success when adding a new word" in {
      val result = buildRepository().upsert("hello", "a form of greeting")
      result.isSuccess mustBe true
    }

    "return Success when adding an existing word" in {
      val result = buildRepository().upsert("apple", "random definition")
      result.isSuccess mustBe true
    }

    "return Failure when an error occurs while upserting a word" in {
      pending
    }
  }
}
