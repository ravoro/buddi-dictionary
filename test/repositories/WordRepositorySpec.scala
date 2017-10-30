package repositories

import org.scalatestplus.play.PlaySpec

class WordRepositorySpec extends PlaySpec {
  "get" must {
    "return None if the given word does not exist" in {
      pending
    }

    "return a Word record with no definitions, if the word exists but has no values in the definitions table" in {
      pending
    }

    "return a Word record with a definition, if there is one definition for it in the definitions table" in {
      pending
    }

    "return a Word record with multiple definitions, if the word has several definitions in the definitions table" in {
      pending
    }
  }

  "getAll" must {
    "return an empty list if there are no words" in {
      pending
    }

    "return a list of all Words" in {

    }
  }

  "upsert" must {
    "inserts a word with given definitions, when the word does not exist in db" in {
      pending
    }

    "inserts newly provided definitions and removes definitions not provided, if the word already exists in the db" in {
      pending
    }
  }
}
