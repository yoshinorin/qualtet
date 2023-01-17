package net.yoshinorin.qualtet.syntax

import net.yoshinorin.qualtet.types.Points
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.syntax.PointsSpec
class PointsSpec extends AnyWordSpec {

  "IntTupleOps" should {

    "be return spreaded values" in {
      assert((0, 1).spread(1, 1, 2) === (0, 2))
      assert((0, 1).spread(1, 1, 1) === (0, 1))
      assert((10, 20).spread(1, 5, 99) === (9, 25))
      assert((10, 20).spread(11, 5, 99) === (0, 25))
    }

  }

}
