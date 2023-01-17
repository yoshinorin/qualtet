package net.yoshinorin.qualtet.syntax

import net.yoshinorin.qualtet.types.Points
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.syntax.PointsSpec
class PointsSpec extends AnyWordSpec {

  "IntTupleOps" should {

    "be return expanded values" in {
      assert((0, 1).expand(1, 1, 2) === (0, 2))
      assert((0, 1).expand(1, 1, 1) === (0, 1))
      assert((10, 20).expand(1, 5, 99) === (9, 25))
      assert((10, 20).expand(11, 5, 99) === (0, 25))
    }

  }

}
