package controllers

import javax.inject._

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.WordRepository

@Singleton
class WordController @Inject()(val messagesApi: MessagesApi,
                               val wordsRepo: WordRepository) extends Controller with I18nSupport {
  def get(word: String) = Action {
    wordsRepo.get(word) match {
      case Some(w) => Ok(views.html.word(w))
      case _ => NotFound
    }
  }
}
