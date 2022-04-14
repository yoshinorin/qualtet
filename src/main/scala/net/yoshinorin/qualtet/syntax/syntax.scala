package net.yoshinorin.qualtet

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._

package object syntax {

  implicit class StringOps(val s: String) {

    private lazy val ignoreCharsRegex = "[!\"#$%&'()-^\\@[;:],./\\=~|`{+*}<>?_、。，．・：；？！゛゜´｀¨＾￣＿]"
    private lazy val ignoreHtmlSpecialCharsRegex = "(&amp;|&#38;|&#169;|&#x27;|&quot;|&#xA0;|&lt;|&gt;|&quot;|&#125;)"

    def stripHtmlTags: String = s.replaceAll("""<(\"[^\"]*\"|'[^']*'|[^'\">])*>""", "")

    def stripHtmlSpecialChars: String = s.replaceAll(ignoreHtmlSpecialCharsRegex, "")

    def hasIgnoreChars: Boolean = ignoreCharsRegex.r.findFirstMatchIn(s) match {
      case None => false
      case Some(_) => true
    }

    def filterIgnoreChars: String = s.replaceAll(ignoreCharsRegex, "")

  }

  implicit final class ValidationCompositions[A](v: EitherT[IO, Throwable, A]) {

    def andThrow: IO[A] = {
      v.value.flatMap {
        case Right(v: A) => IO(v)
        case Left(t: Throwable) => IO.raiseError(t)
      }
    }

  }

  private def getKeyValues(kv: (Option[String], Option[String])): Option[(List[String], List[String])] = {
    kv match {
      case (None, _) => None
      case (_, None) => None
      case (Some(k), Some(v)) =>
        val (x, y) = (k.split(",").map(_.trim).toList, v.split(",").map(_.trim).toList)
        if (x.size =!= y.size) {
          None
        } else {
          Option(x, y)
        }
    }
  }

  implicit class KeyValueCommaSeparatedString(kv: (Option[String], Option[String])) {

    // equally: def zip[A](k: Option[String], v: Option[String])(f: (String, String) => A)
    def zip[A](f: (String, String) => A): Option[List[A]] = {
      getKeyValues(kv) match {
        case None => None
        case Some(kv) => Option(kv._1.zip(kv._2).map(x => f(x._1, x._2)))
      }
    }

    def zipWithGroupBy[A](f: (String, List[(String, String)]) => A): Option[List[A]] = {
      getKeyValues(kv) match {
        case None => None
        case Some(kv) => Option(kv._1.zip(kv._2).groupBy(_._1).map(x => f(x._1, x._2)).toList)
      }
    }
  }

}
