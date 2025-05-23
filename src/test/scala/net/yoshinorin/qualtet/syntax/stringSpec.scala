package net.yoshinorin.qualtet.syntax

import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.errors.UnexpectedException

// testOnly net.yoshinorin.qualtet.syntax.StringSpec
class StringSpec extends AnyWordSpec {

  "stripHtmlTags" should {

    "replace all html tags" in {
      val x = "<html>aiu<p>eo</p><script>kakiku</script>keko<a>sashisu</a>seso<br><code></code></html>owari".stripHtmlTags
      x === "aiueokakikukekosashisusesoowari"
    }

  }

  "stripHtmlSpecialChars" should {

    "replace all html special chars" in {
      val x = "a&amp;b&#38;c&#169;d&#x27;e&quot;f&#xA0;g&lt;h&gt;i&quot;j".stripHtmlSpecialChars
      x === "abcdefghij"
    }

  }

  "hasIgnoreChars" should {

    "detect ignore chars" in {
      assert("test;test".hasIgnoreChars === true)
      assert("\\testtest".hasIgnoreChars === true)
      assert("%testtest".hasIgnoreChars === true)
      assert("testtest%".hasIgnoreChars === true)
      assert("test,test".hasIgnoreChars === true)
      assert("testtest*".hasIgnoreChars === true)
      assert("testtes*t".hasIgnoreChars === true)
      assert("test&test".hasIgnoreChars === true)
      assert("testt@est".hasIgnoreChars === true)
      assert("te/sttest".hasIgnoreChars === true)
      assert("tes.ttest".hasIgnoreChars === true)
      assert("test。test".hasIgnoreChars === true)
      assert("test、test".hasIgnoreChars === true)
      assert("test。test".hasIgnoreChars === true)
      assert("testte、st".hasIgnoreChars === true)
      assert("testtes＿t".hasIgnoreChars === true)
      assert("testtes_t".hasIgnoreChars === true)
      assert("testtes(t".hasIgnoreChars === true)
      assert("testtes)t".hasIgnoreChars === true)
      assert("testtesあt".hasIgnoreChars === false)
      assert("testtesいt".hasIgnoreChars === false)
      assert("testtest".hasIgnoreChars === false)
    }

  }

  "filterIgnoreChars" should {

    "filter ignore chars" in {
      "testtesあt".filterIgnoreChars === "testtesあt"
      "testtesいt".filterIgnoreChars === "testtesいt"
      "test$%^&@!test".filterIgnoreChars === "testtest"
      "\\acb。.def///ghi%_jkl*mn**op$qr{}s\\tuあいう。。えお、かきく!!け!こ".filterIgnoreChars === "acbdefghijklmnopqrstuあいうえおかきくけこ"
    }

  }

  "trimOrThrow" should {

    "not be thrown exception" in {
      assert("test".trimOrThrow(UnexpectedException(detail = "error")) === "test")
      assert(" test ".trimOrThrow(UnexpectedException(detail = "error")) === "test")
    }

    "thrown exception" in {
      assertThrows[UnexpectedException] {
        "".trimOrThrow(UnexpectedException(detail = "error"))
      }

      assertThrows[UnexpectedException] {
        " ".trimOrThrow(UnexpectedException(detail = "error"))
      }
    }

  }

  "position" should {

    "return any word's start and end position in string" in {
      val pos = "あいうえおかきくけこ。あい。あいう。うえお。かきく。あいうおお".position("あいう")
      assert(Seq((0, 3), (14, 17), (26, 29)) === pos)

      val pos2 = "".position("a")
      assert(Seq() === pos2)
    }

  }

}
