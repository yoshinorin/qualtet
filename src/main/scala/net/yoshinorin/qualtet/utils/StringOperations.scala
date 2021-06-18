package net.yoshinorin.qualtet.utils

object StringOps {

  private val ignoreCharsRegex = "[!\"#$%&'()-^\\@[;:],./\\=~|`{+*}<>?_、。，．・：；？！゛゜´｀¨＾￣＿]"

  private val ignoreHtmlSpecialCharsRegex = "(&amp;|&#38;|&#169;|&#x27;|&quot;|&#xA0;|&lt;|&gt;|&quot;|&#125;)"

  implicit class StringOps(val s: String) {

    def stripHtmlTags: String = s.replaceAll("""<(\"[^\"]*\"|'[^']*'|[^'\">])*>""", "")

    def stripHtmlSpecialChars: String = s.replaceAll(ignoreHtmlSpecialCharsRegex, "")

    def hasIgnoreChars: Boolean = ignoreCharsRegex.r.findFirstMatchIn(s) match {
      case None => false
      case Some(_) => true
    }

    def filterIgnoreChars: String = s.replaceAll(ignoreCharsRegex, "")

  }
}
