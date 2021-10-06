package net.yoshinorin.qualtet.domains.models.robots

import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.models.robots.RobotsSpec
class RobotsSpec extends AnyWordSpec {

  "Attributes" should {
    "create instance with valid attribute" in {
      assert(Attributes("nofollow").value == "nofollow")
    }

    "create instance with all valid attributes" in {
      assert(
        Attributes("all, noindex, nofollow, none, noarchive, nosnippet, notranslate, noimageindex").value == "all, noindex, nofollow, none, noarchive, nosnippet, notranslate, noimageindex"
      )
    }

    "create instance with valid attributes pattern one" in {
      assert(
        Attributes("all, noindex, nofollow, none, noarchive, notranslate").value == "all, noindex, nofollow, none, noarchive, notranslate"
      )
    }

    "create instance with valid attributes pattern two" in {
      assert(Attributes("all, noindex, nofollow, none, noarchive").value == "all, noindex, nofollow, none, noarchive")
    }

    "create instance with valid attributes and format them" in {
      assert(Attributes(" all,noindex,    nofollow,none, noarchive  ").value == "all, noindex, nofollow, none, noarchive")
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
