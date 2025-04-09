package net.yoshinorin.qualtet.domains

import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.errors.InvalidPath

// testOnly net.yoshinorin.qualtet.domains.ValueObjectsSpec
class ValueObjectsSpec extends AnyWordSpec {

  "Path" should {

    "appllicable with prefix slush" in {
      val pathString = "/this-is-a-pathあいうえお/q=あ%E3%80%80い&bar"
      val path = Path.apply(pathString)

      assert(path.isInstanceOf[Path])
      assert(path.value === pathString)
    }

    "appllicable without prefix slush" in {
      val pathString = "this-is-a-path"
      val path = Path.apply(pathString)

      assert(path.isInstanceOf[Path])
      assert(path.value === "/" + pathString)
    }

    "throw InvalidPath exception with invalid characters" in {
      assertThrows[InvalidPath] {
        Path.apply("this-is-a-path\u0000")
      }
    }
  }
}
