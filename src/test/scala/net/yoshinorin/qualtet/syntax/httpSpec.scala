package net.yoshinorin.qualtet.syntax

import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.http.request.query.{Limit, Order, Page, Pagination}

// testOnly net.yoshinorin.qualtet.syntax.HttpSpec
class HttpSpec extends AnyWordSpec {

  "http syntax" should {

    "asPagination" should {

      "convert to Pagination" in {
        val result = Map(("page" -> "3"), ("limit" -> "2"), ("order" -> "asc")).asPagination
        assert(result === Pagination(Some(Page(3)), Some(Limit(2)), Some(Order.ASC)))
      }

      "convert to Pagination with default value if key is empty" in {
        val result = Map().asPagination
        assert(result === Pagination(Some(Page(1)), Some(Limit(10)), Some(Order.DESC)))
      }

      "convert to Pagination with default value if key value is invalid" in {
        val result = Map(("page" -> "invalid"), ("limit" -> "invalid"), ("order" -> "invalid")).asPagination
        assert(result === Pagination(Some(Page(1)), Some(Limit(10)), Some(Order.DESC)))
      }

      "convert to Pagination if order param is uppercase" in {
        val result = Map(("order" -> "ASC")).asPagination
        assert(result === Pagination(Some(Page(1)), Some(Limit(10)), Some(Order.ASC)))
      }

    }

  }

}
