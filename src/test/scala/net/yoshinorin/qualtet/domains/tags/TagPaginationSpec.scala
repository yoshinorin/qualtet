package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.domains.pagination.*

import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.tags.TagPaginationSpec
class TagPaginationSpec extends AnyWordSpec {

  "TagsPagination" should {

    val pagination = summon[PaginationQueryParametersOps[TagsPagination]]

    "default instance" in {
      val pagination = TagsPagination()

      assert(pagination.page === 1)
      assert(pagination.limit === 10)
      assert(pagination.offset === 0)
    }

    "instance makeable with PaginationQueryParametersModel" in {
      val requestModel = PaginationQueryParametersModel(Option(Page(10)), Option(Limit(10)), Option(Order.DESC))
      val instance = pagination.make(requestModel)

      assert(instance.page.toInt === 9)
      assert(instance.limit.toInt === 10)
      assert(instance.offset.toInt === 90)
      assert(instance.order === Order.DESC)
    }

    "instance makeable with Option args" in {
      val instance = pagination.make(Option(Page(10)), Option(Limit(10)), Option(Order.ASC))

      assert(instance.page.toInt === 9)
      assert(instance.limit.toInt === 10)
      assert(instance.offset.toInt === 90)
      assert(instance.order === Order.ASC)
    }

    "instance makeable with args" in {
      val instance = pagination.make(Page(10), Limit(10), Order.ASC)

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
      assert(pagination.calcOffset(Some(Page(1))) === 0)
    }
    "calculate offset when page > 1" in {
      assert(pagination.calcOffset(Some(Page(3))) === 20)
    }
  }

}
