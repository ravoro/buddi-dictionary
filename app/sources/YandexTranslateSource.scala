package sources

import javax.inject._

import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import YandexTranslateSource.YandexResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object YandexTranslateSource {

  case class YandexResult(code: Int, lang: String, text: List[String])

  implicit val format = Json.format[YandexResult]
}


@Singleton
class YandexTranslateSource @Inject()(ws: WSClient) {
  private def buildUrl(text: String, lang: String = "en-en"): String =
    s"""https://translate.yandex.net/api/v1.5/tr.json/translate
       |?key=trnsl.1.1.20161128T195751Z.bec3f4c654f96917.83e6de3330e8699e31092052ac0131d06ef35361
       |&text=$text
       |&lang=$lang
       |""".stripMargin.replaceAll("\n", "")

  def get(word: String): Future[Option[String]] = ws.url(buildUrl(word)).get.map { response =>
    response.status match {
      case OK => {
        val json = Json.parse(response.body)
        val resOpt = json.asOpt[YandexResult]
        resOpt.map(_.text.mkString(", "))
      }
      case _ => None
    }
  }
}
