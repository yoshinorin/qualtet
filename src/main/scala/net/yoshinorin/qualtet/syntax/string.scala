package net.yoshinorin.qualtet.syntax

import java.util.Locale
import net.yoshinorin.qualtet.types.Points

trait string {

  private lazy val ignoreCharsRegex = "[!\"#$%&'()-^\\@[;:],./\\=~|`{+*}<>?_、。，．・：；？！゛゜´｀¨＾￣＿]"
  private lazy val ignoreHtmlSpecialCharsRegex = "(&amp;|&#38;|&#169;|&#x27;|&quot;|&#xA0;|&lt;|&gt;|&quot;|&#125;)"

  extension (s: String) {

    def stripHtmlTags: String = s.replaceAll("""<(\"[^\"]*\"|'[^']*'|[^'\">])*>""", "")
    def stripHtmlSpecialChars: String = s.replaceAll(ignoreHtmlSpecialCharsRegex, "")
    def hasIgnoreChars: Boolean = ignoreCharsRegex.r.findFirstMatchIn(s) match {
      case None => false
      case Some(_) => true
    }

    def filterIgnoreChars: String = s.replaceAll(ignoreCharsRegex, "")

    def trimOrThrow(t: Throwable): String = {
      val s2 = s.trim()
      if (s2.isEmpty()) {
        throw t
      }
      s2
    }

    def position(word: String): Seq[Points] = {
      word.r.findAllMatchIn(s).map(m => (m.start, m.end)).toSeq
    }

    def toLower: String = {
      s.toLowerCase(Locale.ROOT)
    }

    def toUpper: String = {
      s.toUpperCase(Locale.ROOT)
    }
  }

}
