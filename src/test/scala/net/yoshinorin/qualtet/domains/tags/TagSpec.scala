package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.fixture.unsafe
import net.yoshinorin.qualtet.domains.errors.InvalidPath
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.tags.TagSpec
class TagSpec extends AnyWordSpec {

  val tagId = TagId("01febb1333pd3431q1aliwofbz")

  "TagId" should {
    "create instance with specific id" in {
      assert(tagId.value === "01febb1333pd3431q1aliwofbz")
    }

    "can not create instance" in {
      assertThrows[IllegalArgumentException] {
        TagId("not-a-ULID")
      }
    }
  }

  "TagPath" should {

    "appllicable with prefix slush" in {
      val pathString = "/this-is-a-pathあいうえお/あ%E3%20%20いbar"
      val path = TagPath.apply(pathString)

      assert(path.isRight)
      assert(path.unsafe.value === pathString)
    }

    "appllicable without prefix slush" in {
      val pathString = "this-is-a-path"
      val path = TagPath.apply(pathString)

      assert(path.isRight)
      assert(path.unsafe.value === "/" + pathString)
    }

    "accept paths with valid characters" in {
      val validPaths = List(
        "/valid/path",
        "/valid/path-with-hyphen",
        "/valid/path_with_underscore",
        "/valid/path.with.dots",
        "/japanese/path/日本語",
        "/path/with/numbers/123",
        "/path with spaces"
      )

      validPaths.foreach { path =>
        val tagPath = TagPath(path)
        assert(tagPath.isRight)
        assert(tagPath.unsafe.value === path)
      }
    }

    "throw InvalidPath exception with invalid characters" in {
      val invalidPaths = List(
        "/invalid/path:with:colon",
        "/invalid/path?with?question",
        "/invalid/path#with#hash",
        "/invalid/path@with@at",
        "/invalid/path!with!exclamation",
        "/invalid/path$with$dollar",
        "/invalid/path&with&ampersand",
        "/invalid/path'with'quote",
        "/invalid/path*with*asterisk",
        "/invalid/path+with+plus",
        "/invalid/path;with;semicolon",
        "/invalid/path=with=equals",
        "/invalid/path<with<less",
        "/invalid/path>with>greater",
        "/invalid/path\"with\"quote",
        "/invalid/path\\with\\backslash",
        "/invalid/path^with^caret",
        "/invalid/path`with`backtick",
        "/invalid/path{with{brace",
        "/invalid/path}with}brace",
        "/invalid/path|with|pipe",
        "/invalid/path~with~tilde"
      )

      invalidPaths.foreach { path =>
        val result = TagPath(path)
        assert(result.isLeft)
        assert(result.left.get.detail === s"Invalid character contains: ${path}")
      }
    }

    "throw InvalidPath exception with invalid percent encoding" in {
      val invalidEncodedPaths = List(
        "/invalid/path/with%",
        "/invalid/path/with%1",
        "/invalid/path/with%XX",
        "/invalid/path/with%ZZ"
      )

      invalidEncodedPaths.foreach { path =>
        val result = TagPath(path)
        assert(result.isLeft)
        assert(result.left.get.detail === s"Invalid percent encoding in path: ${path}")
      }
    }

    "accept valid percent encoding" in {
      val validEncodedPaths = List(
        "/valid/path/with%20space",
        "/valid/path/with%2Fencoded%2Fslash",
        "/valid/path/with%E3%81%82", // "あ" in UTF-8
        "/valid/path/with%3A%3F%23encoded%40special%21chars"
      )

      validEncodedPaths.foreach { path =>
        val tagPath = TagPath(path)
        assert(tagPath.isRight)
        assert(tagPath.unsafe.value === path)
      }
    }

    "add leading slash to path when missing" in {
      val pathWithoutSlash = "path/without/leading/slash"
      val tagPath = TagPath(pathWithoutSlash)
      assert(tagPath.isRight)
      assert(tagPath.unsafe.value === s"/${pathWithoutSlash}")
    }
  }

  "TagPath.unsafe" should {
    "normalize path by adding leading slash" in {
      val path = TagPath.unsafe("scala")
      assert(path.value === "/scala")
    }

    "not add leading slash if already present" in {
      val path = TagPath.unsafe("/scala")
      assert(path.value === "/scala")
    }

    "skip validation for invalid characters" in {
      val path = TagPath.unsafe("invalid:tag")
      assert(path.value === "/invalid:tag")
    }

    "skip validation for invalid percent encoding" in {
      val path = TagPath.unsafe("test%")
      assert(path.value === "/test%")
    }
  }
}
