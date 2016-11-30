import models.WordDefinition
import play.api.data.format.Formatter
import play.api.data.{FormError, Forms}

package object forms {
  val listStringNewLineSeparatedFormat: Formatter[List[String]] = new Formatter[List[String]] {
    def bind(key: String, data: Map[String, String]) = {
      data.get(key).fold[Either[Seq[FormError], List[String]]] {
        Left(Seq(FormError(key, "error.required", Nil)))
      } { value =>
        Right(value.split("\n").map(_.trim).filter(_.nonEmpty).toList)
      }
    }

    def unbind(key: String, value: List[String]) = Map(key -> value.mkString("\n"))
  }

  val listTextNewLineSeparated = Forms.of(listStringNewLineSeparatedFormat)

  val definitionsFormat: Formatter[List[WordDefinition]] = new Formatter[List[WordDefinition]] {
    def bind(key: String, data: Map[String, String]) = {
      listStringNewLineSeparatedFormat.bind(key, data).right.flatMap { strings =>
        def strToDef(str: String) = {
          val split = str.split(" -- ").map(_.trim)
          println(split)
          WordDefinition(split(0), split(1))
        }
        def isStrValid(str: String) = {
          str.split(" -- ").size == 2 && forms.WordForm.validLanguages.contains(strToDef(str).lang)
        }
        if (strings.forall(isStrValid)) {
          Right(strings map strToDef)
        } else {
          Left(Seq(FormError(key, "error.invalid", Nil)))
        }
      }
    }

    def unbind(key: String, value: List[WordDefinition]) = {
      val defsToStrings = value.map(d => s"${d.definition} -- ${d.lang}")
      listStringNewLineSeparatedFormat.unbind(key, defsToStrings)
    }
  }

  val definitions = Forms.of(definitionsFormat)
}
