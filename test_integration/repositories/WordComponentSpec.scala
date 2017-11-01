package repositories

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class WordComponentSpec extends PlaySpec with GuiceOneAppPerTest with FutureAwaits with DefaultAwaitTimeout {

  def buildRepo(words: Seq[String] = Seq())(implicit app: Application): WordRepository = {
    val repo = app.injector.instanceOf(classOf[WordRepository])
    await(Future.sequence(words.map(repo.insertWord)))
    repo
  }

  val newWord = "awesome"
  val oldWords = Seq("hello", "bye")


  "insertWord" must {
    "insert a new record into the database when the given word does not exist" in {
      val repo = buildRepo(oldWords)

      val oldRecord = await(repo.get(newWord))
      await(repo.insertWord(newWord))
      val newRecord = await(repo.get(newWord))

      oldRecord mustBe None
      newRecord.get.word mustBe newWord
    }

    "insert a new record into the database when the given word already exists" in {
      val oldWordsInclusive = oldWords :+ newWord
      val repo = buildRepo(oldWordsInclusive)

      val oldRecord = await(repo.get(newWord))
      await(repo.insertWord(newWord))
      val newRecord = await(repo.get(newWord))

      oldRecord must not be None
      oldRecord.get.word mustBe newWord
      newRecord.get.word mustBe newWord
      oldRecord.get.id must not be newRecord.get.id
    }
  }
}
