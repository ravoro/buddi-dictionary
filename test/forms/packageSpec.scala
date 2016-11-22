package forms

import org.scalatestplus.play.PlaySpec
import play.api.data.FormError

class packageSpec extends PlaySpec {
  "seqStringNewLineSeparatedFormat" must {
    "bind" must {
      "return Left if the provided key is not found in the submitted data" in {
        val data = Map("submission" -> "a submission value")
        val result = seqStringNewLineSeparatedFormat.bind("randomvalue", data)
        val expected = Left(Seq(FormError("randomvalue", "error.required", Nil)))
        result mustBe expected
      }

      "return Right with list of single string, if the value is not a string separated by newline" in {
        val data = Map("submission" -> "a submission value")
        val result = seqStringNewLineSeparatedFormat.bind("submission", data)
        val expected = Right(Seq(data("submission")))
        result mustBe expected
      }

      "return Right with list of string separated by newlines" in {
        val data = Map("submission" -> "a submission value\nanother line\nthird")
        val result = seqStringNewLineSeparatedFormat.bind("submission", data)
        val expected = Right(Seq("a submission value", "another line", "third"))
        result mustBe expected
      }

      "return Right with list of string not including empty strings" in {
        val data = Map("submission" -> "a submission value\nanother line\n\n\n\nthird\n\n")
        val result = seqStringNewLineSeparatedFormat.bind("submission", data)
        val expected = Right(Seq("a submission value", "another line", "third"))
        result mustBe expected
      }

      "return Right with an empty list if the given value is empty" in {
        val data = Map("submission" -> "")
        val result = seqStringNewLineSeparatedFormat.bind("submission", data)
        val expected = Right(Seq())
        result mustBe expected
      }

      "return Right with all string in resulting list being stripped of white space characters" in {
        val data = Map("submission" -> "  a submission value \n\r  \t another line   \n \nthird\r")
        val result = seqStringNewLineSeparatedFormat.bind("submission", data)
        val expected = Right(Seq("a submission value", "another line", "third"))
        result mustBe expected
      }
    }

    "unbind" must {
      "return an empty string, if the value is an empty list" in {
        val data = Seq()
        val result = seqStringNewLineSeparatedFormat.unbind("submission", data)
        val expected = Map("submission" -> "")
        result mustBe expected
      }

      "return a string, if the value is a list with one item" in {
        val data = Seq("a submission value")
        val result = seqStringNewLineSeparatedFormat.unbind("submission", data)
        val expected = Map("submission" -> data(0))
        result mustBe expected
      }

      "return a string separated by newlines, if the value is a list with multiple items" in {
        val data = Seq("a submission value", "another line", "third")
        val result = seqStringNewLineSeparatedFormat.unbind("submission", data)
        val expected = Map("submission" -> data.mkString("\n"))
        result mustBe expected
      }
    }
  }
}
