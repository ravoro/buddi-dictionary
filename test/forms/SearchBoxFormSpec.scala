package forms

import org.scalatestplus.play.PlaySpec
import SearchBoxForm.{form => searchForm}

class SearchBoxFormSpec extends PlaySpec {
  "search box form" must {
    "trim whitespaces of the query value" in {
      val data = Map("query" -> "   hello ")
      searchForm.bind(data).fold(
        formWithErrors => fail("valid submission should not fail"),
        success => {
          success.query mustNot be(data("query"))
          success.query mustBe data("query").trim
        }
      )
    }

    "not modify the query value if there are no whitespaces to trim" in {
      val data = Map("query" -> "hello")
      searchForm.bind(data).fold(
        formWithErrors => fail("valid submission should not fail"),
        success => {
          success.query mustBe data("query")
        }
      )
    }
  }

}
