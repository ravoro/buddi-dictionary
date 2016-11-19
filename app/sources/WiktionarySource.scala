package sources

import javax.inject._

import play.api.libs.ws.WSClient
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class WiktionarySource @Inject()(ws: WSClient) extends Controller {
  private def buildUrl(word: String): String = s"https://en.wiktionary.org/wiki/$word?action=render"

  def get(word: String): Future[Option[String]] = ws.url(buildUrl(word)).get.map { response =>
    response.status match {
      case OK => Some(response.body)
      case _ => None
    }
  }
}
