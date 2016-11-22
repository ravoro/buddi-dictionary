import play.api.data.format.Formatter
import play.api.data.{FormError, Forms}

package object forms {
  val seqStringNewLineSeparatedFormat: Formatter[Seq[String]] = new Formatter[Seq[String]] {
    def bind(key: String, data: Map[String, String]) = {
      data.get(key).fold[Either[Seq[FormError], Seq[String]]] {
        Left(Seq(FormError(key, "error.required", Nil)))
      } { value =>
        Right(value.split("\n").map(_.trim).filter(_.nonEmpty).toSeq)
      }
    }

    def unbind(key: String, value: Seq[String]) = Map(key -> value.mkString("\n"))
  }

  val seqTextNewLineSeparated = Forms.of(seqStringNewLineSeparatedFormat)
}
