package sources

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status._
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

import scala.concurrent.Future

class WiktionarySourceSpec extends PlaySpec with MockitoSugar with ScalaFutures {
  def buildSource(status: Int = OK, body: String = ""): WiktionarySource = {
    val mockRes = mock[WSResponse]
    when(mockRes.status).thenReturn(status)
    when(mockRes.body).thenReturn(body)

    val mockReq = mock[WSRequest]
    when(mockReq.get()).thenReturn(Future.successful(mockRes))

    val mockWS = mock[WSClient]
    when(mockWS.url(anyString())).thenReturn(mockReq)

    new WiktionarySource(mockWS)
  }

  val mockWord = "hello"
  val mockBody = "a form of greeting"

  "get" must {
    "Return the response body when the source responds with status 200" in {
      val mockSource = buildSource(OK, mockBody)
      whenReady(mockSource.get(mockWord)) { resOpt =>
        resOpt mustBe Some(mockBody)
      }
    }

    "Return None when the source responds with a non-200 status" in {
      val statuses = Seq(CREATED, NO_CONTENT, FOUND, NOT_MODIFIED, BAD_REQUEST, NOT_FOUND, INTERNAL_SERVER_ERROR)
      for (status <- statuses) {
        val mockSource = buildSource(status, mockBody)
        whenReady(mockSource.get(mockWord)) { resOpt =>
          resOpt mustBe None
        }
      }
    }
  }
}
