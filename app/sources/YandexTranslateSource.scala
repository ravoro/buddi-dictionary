package sources

import javax.inject._

import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class YandexTranslateSource @Inject()(ws: WSClient) extends Controller {
  private def buildUrl(text: String, lang: String = "en-en"): String =
    s"""https://translate.yandex.net/api/v1.5/tr.json/translate
        |?key=trnsl.1.1.20161128T195751Z.bec3f4c654f96917.83e6de3330e8699e31092052ac0131d06ef35361
        |&text=$text
        |&lang=$lang
        |""".stripMargin.replaceAll("\n", "")

  private case class YandexResult(code: Int, lang: String, text: List[String])

  private implicit val format = Json.format[YandexResult]

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
