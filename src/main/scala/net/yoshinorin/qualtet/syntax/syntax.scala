package net.yoshinorin.qualtet

import cats.data.EitherT
import cats.effect.IO

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
}
