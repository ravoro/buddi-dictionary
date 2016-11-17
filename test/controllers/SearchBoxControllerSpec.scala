package controllers

import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers._

class SearchBoxControllerSpec extends PlaySpec {
  val mockFormData = Map("query" -> "hello")

  def buildController() = {
    new SearchBoxController
  }

  "search" must {
    "return 303 and direct user to the word page of the provided word" in {
      val request = FakeRequest().withFormUrlEncodedBody(mockFormData.toSeq: _*)
      val result = buildController().search()(request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.WordController.get(mockFormData("query")).url)
    }

    "return 303 and direct user to the all words page if the provided word is empty" in {
      val formData = Map("query" -> "")
      val request = FakeRequest().withFormUrlEncodedBody(formData.toSeq: _*)
      val result = buildController().search()(request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.WordController.getAll.url)
    }
  }
}
