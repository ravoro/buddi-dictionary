package sources

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import sources.YandexDictionarySource._

import scala.concurrent.Future


class YandexDictionarySourceSpec extends PlaySpec with MockitoSugar with ScalaFutures {
  def buildSource(status: Int = OK, body: String = ""): YandexDictionarySource = {
    val mockRes = mock[WSResponse]
    when(mockRes.status).thenReturn(status)
    when(mockRes.body).thenReturn(body)

    val mockReq = mock[WSRequest]
    when(mockReq.get()).thenReturn(Future.successful(mockRes))

    val mockWS = mock[WSClient]
    when(mockWS.url(anyString())).thenReturn(mockReq)

    new YandexDictionarySource(mockWS)
  }

  val mockWord = "hello"
  val mockDefs = List("hey", "hi", "a form of greeting")
  val mockTrDefs = mockDefs.map(Tr(_, "noun"))

  def mockRes(trs: List[Tr] = mockTrDefs) = YandexResult(List(Def(mockWord, "noun", trs)))

  def mockBody(res: YandexResult = mockRes()) = Json.toJson(res).toString

  "get" must {
    "Return the definitions as a comma separated list when the source responds with status 200" in {
      val mockSource = buildSource(OK, mockBody())
      whenReady(mockSource.get(mockWord)) { resOpt =>
        resOpt.get.split(", ") mustBe mockDefs
      }
    }

    "Return None when the source responds with a non-200 status" in {
      val statuses = Seq(CREATED, NO_CONTENT, FOUND, NOT_MODIFIED, BAD_REQUEST, NOT_FOUND, INTERNAL_SERVER_ERROR)
      for (status <- statuses) {
        val mockSource = buildSource(status, mockBody())
        whenReady(mockSource.get(mockWord)) { resOpt =>
          resOpt mustBe None
        }
      }
    }

    "Return the synonyms as part of the definitions list" in {
      val mockDefsWithSyns = List(
        Tr("hey", "noun", syn = Some(List(BaseText("hi")))),
        Tr("yo", "noun", syn = Some(List(BaseText("howdy"), BaseText("areet")))))
      val mockResWithSyns = mockRes(mockDefsWithSyns)

      val mockSource = buildSource(OK, mockBody(mockResWithSyns))
      whenReady(mockSource.get(mockWord)) { resOpt =>
        resOpt.get.split(", ") mustBe List("hey", "hi", "yo", "howdy", "areet")
      }
    }
  }
}
