package forms

import forms.SearchBoxForm.{form => searchForm}
import org.scalatestplus.play.PlaySpec

class SearchBoxFormSpec extends PlaySpec {
  val mockData = Map("query" -> "hello")
  val mockSearchBox = SearchBox(mockData("query"))

  "SearchBoxForm mapping" must {
    "bind raw data to SearchBox type" in {
      searchForm.mapping.bind(mockData).fold(
        _ => fail("binding should not fail"),
        searchBox => {
          searchBox mustBe mockSearchBox
        }
      )
    }

    "unbind SearchBox type to raw data" in {
      searchForm.mapping.unbind(mockSearchBox) mustBe mockData
    }
  }

  "SearchBoxForm trim functionality" must {
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
      searchForm.bind(mockData).fold(
        formWithErrors => fail("valid submission should not fail"),
        success => {
          success.query mustBe mockData("query")
        }
      )
    }
  }
}
