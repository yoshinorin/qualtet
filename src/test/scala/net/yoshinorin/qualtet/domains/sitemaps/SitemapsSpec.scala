package net.yoshinorin.qualtet.domains.sitemaps

import net.yoshinorin.qualtet.domains.sitemaps.{LastMod, Loc}
import net.yoshinorin.qualtet.domains.errors.InvalidLastMod
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.fixture.unsafe
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
      assert(LastMod("1620738897").unsafe.value === "2021-05-11")
    }
    "invalid value (NOT a unixtime)" in {
      assert(LastMod("1620738").unsafe.value === "1970-01-19")
    }
    "invalid value (can not toLong)" in {
      val result = LastMod("aaaa")
      assert(result.isLeft)
      assert(result.left.toOption.get.isInstanceOf[InvalidLastMod])
    }
  }

  "LastMod.fromTrusted" should {
    "not modify the input" in {
      val lastMod = LastMod.fromTrusted("2024-01-01")
      assert(lastMod.value === "2024-01-01")
    }

    "skip validation for invalid format" in {
      val lastMod = LastMod.fromTrusted("invalid-date")
      assert(lastMod.value === "invalid-date")
    }

    "skip validation for empty string" in {
      val lastMod = LastMod.fromTrusted("")
      assert(lastMod.value === "")
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
      """.stripMargin.replaceNewlineAndSpace

      val urls =
        Seq(
          Url(Loc("https://example.com/aaa/bbb"), LastMod("1620738897").unsafe),
          Url(Loc("https://example.com/ccc/ddd"), LastMod("1620938897").unsafe)
        ).asJson.replaceNewlineAndSpace

      assert(expectJson === urls)
    }
  }

}
