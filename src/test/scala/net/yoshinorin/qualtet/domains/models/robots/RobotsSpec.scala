package net.yoshinorin.qualtet.domains.models.robots

import io.circe.syntax._
import net.yoshinorin.qualtet.domains.models.contents.ContentId
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.models.robots.RobotsSpec
class RobotsSpec extends AnyWordSpec {

  "Robots" should {
    "create instance" in {
      val robots = Robots(ContentId("01febb1333pd3431q1a1e00fbt"), Attributes("all, noindex, nofollow, none, noarchive, nosnippet, notranslate, noimageindex"))
      assert(robots.contentId.value == "01febb1333pd3431q1a1e00fbt")
      assert(robots.attributes.value == "all, noarchive, nofollow, noimageindex, noindex, none, nosnippet, notranslate")
    }

    "as JSON" in {
      val expectJson =
        """
          |{
          |  "contentId" : "01febb1333pd3431q1a1e00fbt",
          |  "attributes" : "all, noarchive, nofollow, noimageindex, noindex, none, nosnippet, notranslate"
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      val json = Robots(
        ContentId("01febb1333pd3431q1a1e00fbt"),
        Attributes("all, noarchive, nofollow, noimageindex, noindex, none, nosnippet, notranslate")
      ).asJson.toString.replaceAll("\n", "").replaceAll(" ", "")

      //NOTE: failed equally compare
      assert(json.contains(expectJson))
    }
  }

  "Attributes" should {
    "create instance with valid attribute" in {
      assert(Attributes("nofollow").value == "nofollow")
    }

    "create instance with all valid attributes and result are sorted" in {
      assert(
        Attributes("all, noindex, nofollow, none, noarchive, nosnippet, notranslate, noimageindex").value == "all, noarchive, nofollow, noimageindex, noindex, none, nosnippet, notranslate"
      )
    }

    "create instance with valid attributes pattern one" in {
      assert(
        Attributes("all, noindex, nofollow, none, noarchive, notranslate").value == "all, noarchive, nofollow, noindex, none, notranslate"
      )
    }

    "create instance with valid attributes pattern two" in {
      assert(Attributes("all, noindex, nofollow, none, noarchive").value == "all, noarchive, nofollow, noindex, none")
    }

    "create instance with valid attributes and format them" in {
      assert(Attributes(" all,noindex,    nofollow,none, noarchive  ").value == "all, noarchive, nofollow, noindex, none")
    }

    "can not create instance with invalid attribute" in {
      // TODO: declare exception
      assertThrows[Exception] {
        Attributes("invalid-attribute")
      }
    }

    "can not create instance with includes invalid attribute" in {
      // TODO: declare exception
      assertThrows[Exception] {
        Attributes("all, noindex, nofollow, invalid, none, noarchive, notranslate")
      }
    }

    "can not create instance with includes empty attribute start of string" in {
      // TODO: declare exception
      assertThrows[Exception] {
        Attributes(",all, noindex, nofollow, none, noarchive")
      }
    }

    "can not create instance with includes empty attribute end of string" in {
      // TODO: declare exception
      assertThrows[Exception] {
        Attributes("all, noindex, nofollow, none, noarchive,")
      }
    }
  }

}
