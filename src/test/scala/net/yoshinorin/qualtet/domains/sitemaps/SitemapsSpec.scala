package net.yoshinorin.qualtet.domains.sitemaps

import net.yoshinorin.qualtet.domains.sitemaps.{LastMod, Loc}
import net.yoshinorin.qualtet.syntax.*
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.sitemaps.SitemapsSpec
class SitemapsSpec extends AnyWordSpec {

  "Loc" should {
    "valid value" in {
      assert(Loc("https://example.com/test/post").value === "https://example.com/test/post")
    }
    "invalid value" ignore {
      // TODO: write test
    }
  }

  "LastMod" should {
    "valid value" in {
      assert(LastMod("1620738897").value === "2021-05-11")
    }
    "invalid value (NOT a unixtime)" in {
      // TODO: throw exception
      assert(LastMod("1620738").value === "1970-01-19")
    }
    "invalid value (can not toLong)" in {
      // TODO: throw exception
      assertThrows[NumberFormatException] {
        LastMod("aaaa")
      }
    }
  }

  "Url(Sitemaps)" should {
    "as JSON" in {

      val expectJson =
        """
          |[
          |  {
          |    "loc" : "https://example.com/aaa/bbb",
          |    "lastMod" : "2021-05-11"
          |  },
          |  {
          |    "loc" : "https://example.com/ccc/ddd",
          |    "lastMod" : "2021-05-13"
          |  }
          |]
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      val urls =
        Seq(
          Url(Loc("https://example.com/aaa/bbb"), LastMod("1620738897")),
          Url(Loc("https://example.com/ccc/ddd"), LastMod("1620938897"))
        ).asJson.replaceAll("\n", "").replaceAll(" ", "")

      assert(expectJson === urls)
    }
  }

}
