package sources

import javax.inject._

import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import YandexDictionarySource.YandexResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object YandexDictionarySource {

  case class YandexResult(`def`: List[Def])

  case class Def(text: String, pos: String, tr: List[Tr])

  case class Tr(text: String,
                pos: String,
                syn: Option[List[BaseText]] = None,
                mean: Option[List[BaseText]] = None,
                ex: Option[List[Ex]] = None)

  case class Ex(text: String, tr: List[BaseText])

  case class BaseText(text: String)

  implicit val formatBaseText = Json.format[BaseText]
  implicit val formatEx = Json.format[Ex]
  implicit val formatTr = Json.format[Tr]
  implicit val formatDef = Json.format[Def]
  implicit val formatYandexResult = Json.format[YandexResult]
}


@Singleton
class YandexDictionarySource @Inject()(ws: WSClient) {
  private def buildUrl(text: String, lang: String = "en-en"): String =
    s"""https://dictionary.yandex.net/api/v1/dicservice.json/lookup
       |?key=dict.1.1.20161128T230211Z.10e66141871b65ef.fb53101a964a5d0cf3a338416d0658c19687dd24
       |&text=$text
       |&lang=$lang
       |""".stripMargin.replaceAll("\n", "")

  def get(word: String): Future[Option[String]] = ws.url(buildUrl(word)).get.map { response =>
    response.status match {
      case OK => {
        val json = Json.parse(response.body)
        val resOpt = json.asOpt[YandexResult]

        val nonEmptyDefOpt = resOpt.filter(_.`def`.nonEmpty)

        val defsOpt = nonEmptyDefOpt.map { res =>
          val defsLists = for {
            definition <- res.`def`
            translation <- definition.tr
            synonyms = translation.syn.fold(List.empty[String])(_.map(_.text))
          } yield translation.text :: synonyms
          defsLists.flatten
        }

        defsOpt.map(_.mkString(", "))
      }
      case _ => None
    }
  }
}
