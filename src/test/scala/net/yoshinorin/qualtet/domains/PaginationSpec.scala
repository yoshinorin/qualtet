package net.yoshinorin.qualtet.domains.pagination

import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.PaginationSpec
class PaginationSpec extends AnyWordSpec {

  "Page" should {
    "Computable" in {
      val a = Page(3)
      val b = Page(3)

      assert(a + b === 6)
      assert(a - b === 0)
      assert(a * b === 9)
      assert(a / b === 1)
    }
  }

  "Limit" should {
    "Computable" in {
      val a = Limit(3)
      val b = Limit(3)

      assert(a + b === 6)
      assert(a - b === 0)
      assert(a * b === 9)
      assert(a / b === 1)
    }
  }

}
