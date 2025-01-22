package net.yoshinorin.qualtet.domains

import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.PaginationSpec
class PaginationSpec extends AnyWordSpec {

  "ArticlesPagination" should {

    val pagination = summon[PaginationOps[ArticlesPagination]]

    "default instance" in {
      val pagination = ArticlesPagination()

      assert(pagination.page === 1)
      assert(pagination.limit === 10)
      assert(pagination.offset === 0)
    }

    "instance makeable with PaginationRequestModel" in {
      val requestModel = PaginationRequestModel(Option(Page(10)), Option(Limit(10)), Option(Order.DESC))
      val instance = pagination.make(requestModel)

      assert(instance.page.toInt === 9)
      assert(instance.limit.toInt === 10)
      assert(instance.offset.toInt === 90)
      assert(instance.order === Order.DESC)
    }

    "instance makeable with args" in {
      val instance = pagination.make(Option(Page(10)), Option(Limit(10)), Option(Order.ASC))

      assert(instance.page.toInt === 9)
      assert(instance.limit.toInt === 10)
      assert(instance.offset.toInt === 90)
      assert(instance.order === Order.ASC)
    }

    "instance makeable with default args" in {
      val instance = pagination.make(Option(Page(10)), Option(Limit(10)), None)

      assert(instance.page.toInt === 9)
      assert(instance.limit.toInt === 10)
      assert(instance.offset.toInt === 90)
      assert(instance.order === Order.DESC)
    }

    "calculate default page if None" in {
      assert(pagination.calcPage(None).toInt === 0)
    }
    "calculate valid page if Some" in {
      assert(pagination.calcPage(Some(Page(5))).toInt === 4)
    }
    "calculate limit capped at 10" in {
      assert(pagination.calcLimit(Some(Limit(15))).toInt === 10)
    }
    "calculate offset when page is 1" in {
      val pagination = summon[PaginationOps[ArticlesPagination]]
      assert(pagination.calcOffset(Some(Page(1))) === 0)
    }
    "calculate offset when page > 1" in {
      assert(pagination.calcOffset(Some(Page(3))) === 20)
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
