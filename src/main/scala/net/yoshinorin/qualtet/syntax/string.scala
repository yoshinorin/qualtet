package net.yoshinorin.qualtet.syntax

trait string {

  implicit final class StringOps(val s: String) {
    private lazy val ignoreCharsRegex = "[!\"#$%&'()-^\\@[;:],./\\=~|`{+*}<>?_、。，．・：；？！゛゜´｀¨＾￣＿]"
    private lazy val ignoreHtmlSpecialCharsRegex = "(&amp;|&#38;|&#169;|&#x27;|&quot;|&#xA0;|&lt;|&gt;|&quot;|&#125;)"

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

    def position(word: String): Seq[(Int, Int)] = {
      word.r.findAllMatchIn(s).map(m => (m.start, m.end)).toSeq
    }
  }

}
