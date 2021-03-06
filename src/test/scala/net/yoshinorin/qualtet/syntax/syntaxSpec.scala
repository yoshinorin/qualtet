package net.yoshinorin.qualtet.syntax

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.syntax._

// testOnly net.yoshinorin.qualtet.utils.StringOpsSpec
class StringOpsSpec extends AnyWordSpec {

  "StringOps - stripHtmlTags" should {

    "replace all html tags" in {
      val x = "<html>aiu<p>eo</p><script>kakiku</script>keko<a>sashisu</a>seso<br><code></code></html>owari".stripHtmlTags
      x mustBe "aiueokakikukekosashisusesoowari"
    }

  }

  "StringOps - stripHtmlSpecialChars" should {

    "replace all html special chars" in {
      val x = "a&amp;b&#38;c&#169;d&#x27;e&quot;f&#xA0;g&lt;h&gt;i&quot;j".stripHtmlSpecialChars
      x mustBe "abcdefghij"
    }

  }

  "StringOps - hasIgnoreChars" should {

    "detected ignore chars" in {
      "test;test".hasIgnoreChars mustBe true
      "\\testtest".hasIgnoreChars mustBe true
      "%testtest".hasIgnoreChars mustBe true
      "testtest%".hasIgnoreChars mustBe true
      "test,test".hasIgnoreChars mustBe true
      "testtest*".hasIgnoreChars mustBe true
      "testtes*t".hasIgnoreChars mustBe true
      "test&test".hasIgnoreChars mustBe true
      "testt@est".hasIgnoreChars mustBe true
      "te/sttest".hasIgnoreChars mustBe true
      "tes.ttest".hasIgnoreChars mustBe true
      "test。test".hasIgnoreChars mustBe true
      "test、test".hasIgnoreChars mustBe true
      "test。test".hasIgnoreChars mustBe true
      "testte、st".hasIgnoreChars mustBe true
      "testtes＿t".hasIgnoreChars mustBe true
      "testtes_t".hasIgnoreChars mustBe true
      "testtes(t".hasIgnoreChars mustBe true
      "testtes)t".hasIgnoreChars mustBe true
      "testtesあt".hasIgnoreChars mustBe false
      "testtesいt".hasIgnoreChars mustBe false
      "testtest".hasIgnoreChars mustBe false
    }

  }

  "StringOps - filterIgnoreChars" should {

    "filterd ignore chars" in {
      "testtesあt".filterIgnoreChars mustBe "testtesあt"
      "testtesいt".filterIgnoreChars mustBe "testtesいt"
      "test$%^&@!test".filterIgnoreChars mustBe "testtest"
      "\\acb。.def///ghi%_jkl*mn**op$qr{}s\\tuあいう。。えお、かきく!!け!こ".filterIgnoreChars mustBe "acbdefghijklmnopqrstuあいうえおかきくけこ"
    }

  }

}
