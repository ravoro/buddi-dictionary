package controllers

import org.jsoup.Jsoup
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers._

class HomeControllerSpec extends PlaySpec {
  "home" must {
    "return 200 and display the home page" in {
      val result = new HomeController(mockMessagesApi).get()(FakeRequest())
      status(result) mustBe OK
      val doc = Jsoup.parse(contentAsString(result))
      doc.select("body#page-home").size mustBe 1
    }
  }
}
