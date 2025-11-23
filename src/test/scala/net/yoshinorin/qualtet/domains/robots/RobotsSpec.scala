package net.yoshinorin.qualtet.domains.robots

import net.yoshinorin.qualtet.fixture.unsafe
import net.yoshinorin.qualtet.domains.errors.InvalidAttributes
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.robots.RobotsSpec
class RobotsSpec extends AnyWordSpec {

  "Robots" should {
    "create instance" in {
      val robots = Robots(contentId, fullRobotsAttributes)
      assert(robots.contentId.value === "01febb1333pd3431q1a1e00fbt")
      assert(robots.attributes.value === "all, noarchive, nofollow, noimageindex, noindex, none, nosnippet, notranslate")
    }

    "as JSON" in {
      val expectJson =
        """
          |{
          |  "contentId" : "01febb1333pd3431q1a1e00fbt",
          |  "attributes" : "all, noarchive, nofollow, noimageindex, noindex, none, nosnippet, notranslate"
          |}
      """.stripMargin.replaceNewlineAndSpace

      val json =
        Robots(
          contentId,
          Attributes("all, noarchive, nofollow, noimageindex, noindex, none, nosnippet, notranslate").unsafe
        ).asJson.replaceNewlineAndSpace

      // NOTE: failed equally compare
      assert(json.contains(expectJson))
    }
  }

  "Attributes" should {
    "create instance with valid attribute" in {
      assert(Attributes("nofollow").unsafe.value === "nofollow")
    }

    "create instance with all valid attributes and result are sorted" in {
      assert(
        fullRobotsAttributes.value === "all, noarchive, nofollow, noimageindex, noindex, none, nosnippet, notranslate"
      )
    }

    "create instance with valid attributes pattern one" in {
      assert(
        Attributes("all, noindex, nofollow, none, noarchive, notranslate").unsafe.value === "all, noarchive, nofollow, noindex, none, notranslate"
      )
    }

    "create instance with valid attributes pattern two" in {
      assert(Attributes("all, noindex, nofollow, none, noarchive").unsafe.value === "all, noarchive, nofollow, noindex, none")
    }

    "create instance with valid attributes and format them" in {
      assert(Attributes(" all,noindex,    nofollow,none, noarchive  ").unsafe.value === "all, noarchive, nofollow, noindex, none")
    }

    "can not create instance with invalid attribute" in {
      val result = Attributes("invalid-attribute")
      assert(result.isLeft)
      assert(result.left.get.isInstanceOf[InvalidAttributes])
    }

    "can not create instance with includes invalid attribute" in {
      val result = Attributes("all, noindex, nofollow, invalid, none, noarchive, notranslate")
      assert(result.isLeft)
      assert(result.left.get.isInstanceOf[InvalidAttributes])
    }

    "can not create instance with includes empty attribute start of string" in {
      val result = Attributes(",all, noindex, nofollow, none, noarchive")
      assert(result.isLeft)
      assert(result.left.get.isInstanceOf[InvalidAttributes])
    }

    "can not create instance with includes empty attribute end of string" in {
      val result = Attributes("all, noindex, nofollow, none, noarchive,")
      assert(result.isLeft)
      assert(result.left.get.isInstanceOf[InvalidAttributes])
    }
  }

  "Attributes.fromTrusted" should {
    "not modify the input" in {
      val attrs = Attributes.fromTrusted("noindex, nofollow")
      assert(attrs.value === "noindex, nofollow")
    }

    "skip validation for invalid attributes" in {
      val attrs = Attributes.fromTrusted("invalid-attr")
      assert(attrs.value === "invalid-attr")
    }

    "skip validation for malformed input" in {
      val attrs = Attributes.fromTrusted("noindex,")
      assert(attrs.value === "noindex,")
    }
  }

}
