import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.{Configuration, Environment}

package object controllers {
  val mockMessagesApi = new DefaultMessagesApi(
    Environment.simple(),
    Configuration.reference,
    new DefaultLangs(Configuration.reference))
}
