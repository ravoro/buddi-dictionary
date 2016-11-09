package repositories

import models.Word
import org.scalatestplus.play.PlaySpec

class WordRepositorySpec extends PlaySpec {
  val mockDb = Map(
    "apple" -> "type of fruit",
    "ball" -> "item for playing")

  def buildRepository(records: List[(String, String)] = mockDb.toList) = {
    val repo = new WordRepository()
    records.foreach { case (word, definition) => repo.add(word, definition) }
    repo
  }

  "add" must {
    "return Success when adding a new word" in {
      val result = buildRepository().add("hello", "a form of greeting")
      result.isSuccess mustBe true
    }

    "return Failure when adding an existing word" in {
      val result = buildRepository().add("apple", "random definition")
      result.isFailure mustBe true
    }
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
}
