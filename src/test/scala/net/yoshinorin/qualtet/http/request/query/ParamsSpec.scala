package net.yoshinorin.qualtet.http.request.query

import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.http.request.query.ParamSpec
class ParamSpec extends AnyWordSpec {

  "ArticlesPagination" should {
    "default instance" in {
      val pagination = ArticlesPagination()

      assert(pagination.page === 1)
      assert(pagination.limit === 10)
      assert(pagination.offset === 0)
    }

    /* TODO:
    "limit is bigger than 10" in {
      val pagination = ArticlesPagination(page = 1, limit = 11)

      assert(pagination.limit === 10)
    }

    "page is bigger than 1" in {
      val pagination = ArticlesPagination(page = 5)

      assert(pagination.page === 5)
      assert(pagination.offset === 50)
    }
     */
  }

  "PaginationHelper" should {
    "calculate default page if None" in {
      assert(PaginationHelper.calcPage(None).toInt === 0)
    }
    "calculate valid page if Some" in {
      assert(PaginationHelper.calcPage(Some(Page(5))).toInt === 4)
    }
    "calculate limit capped at 10" in {
      assert(PaginationHelper.calcLimit(Some(Limit(15))).toInt === 10)
    }
    "calculate offset when page is 1" in {
      assert(PaginationHelper.calcOffset(Some(Page(1))) === 0)
    }
    "calculate offset when page > 1" in {
      assert(PaginationHelper.calcOffset(Some(Page(3))) === 20)
    }
  }

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
