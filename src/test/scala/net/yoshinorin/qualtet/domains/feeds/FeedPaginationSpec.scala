package net.yoshinorin.qualtet.domains.feeds

import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.pagination.*

// testOnly net.yoshinorin.qualtet.domains.feeds.TagPaginationSpec
class TagPaginationSpec extends AnyWordSpec {

  "FeedsPagination" should {

    val pagination = summon[PaginationQueryParametersOps[FeedsPagination]]

    "default instance" in {
      val pagination = FeedsPagination()

      assert(pagination.page === 1)
      assert(pagination.limit === 5)
      assert(pagination.offset === 0)
    }

    "instance makeable with PaginationQueryParametersModel" in {
      val requestModel = PaginationQueryParametersModel(Option(Page(10)), Option(Limit(10)), Option(Order.DESC))
      val instance = pagination.make(requestModel)

      // NOTE: FeedsPagination always uses page=1 and offset=0, but respects the passed limit.
      assert(instance.page.toInt === 1)
      assert(instance.limit.toInt === 10)
      assert(instance.offset.toInt === 0)
      assert(instance.order === Order.DESC)
    }

    "instance makeable with Option args" in {
      val instance = pagination.make(Option(Page(10)), Option(Limit(10)), Option(Order.ASC))

      // NOTE: FeedsPagination always uses page=1 and offset=0, but respects the passed limit.
      assert(instance.page.toInt === 1)
      assert(instance.limit.toInt === 10)
      assert(instance.offset.toInt === 0)
      assert(instance.order === Order.DESC)
    }

    "instance makeable with args" in {
      val instance = pagination.make(Page(10), Limit(10), Order.ASC)

      // NOTE: FeedsPagination always uses page=1 and offset=0, but respects the passed limit.
      assert(instance.page.toInt === 1)
      assert(instance.limit.toInt === 10)
      assert(instance.offset.toInt === 0)
      assert(instance.order === Order.DESC)
    }

    "instance makeable with default args" in {
      val instance = pagination.make(Option(Page(10)), Option(Limit(10)), None)

      // NOTE: FeedsPagination always uses page=1 and offset=0, but respects the passed limit.
      assert(instance.page.toInt === 1)
      assert(instance.limit.toInt === 10)
      assert(instance.offset.toInt === 0)
      assert(instance.order === Order.DESC)
    }

    "instance makeable with None limit in PaginationQueryParametersModel" in {
      val requestModel = PaginationQueryParametersModel(None, None, None)
      val instance = pagination.make(requestModel)

      assert(instance.page.toInt === 1)
      assert(instance.limit.toInt === 5)
      assert(instance.offset.toInt === 0)
      assert(instance.order === Order.DESC)
    }

    "instance makeable with None limit in Option args" in {
      val instance = pagination.make(None, None, None)

      assert(instance.page.toInt === 1)
      assert(instance.limit.toInt === 5)
      assert(instance.offset.toInt === 0)
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
      assert(pagination.calcOffset(Some(Page(1))) === 0)
    }
    "calculate offset when page > 1" in {
      assert(pagination.calcOffset(Some(Page(3))) === 20)
    }
  }

}
