package net.yoshinorin.qualtet.syntax

import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.http.request.query.{Limit, Order, Page, RequestQueryParamater}

// testOnly net.yoshinorin.qualtet.syntax.HttpSpec
class HttpSpec extends AnyWordSpec {

  "http syntax" should {

    "asRequestQueryParamater" should {

      "convert to RequestQueryParamater" in {
        val result = Map(("page" -> "3"), ("limit" -> "2"), ("order" -> "asc")).asRequestQueryParamater
        assert(result === RequestQueryParamater(Some(Page(3)), Some(Limit(2)), Some(Order.ASC)))
      }

      "convert to RequestQueryParamater with default value if key is empty" in {
        val result = Map().asRequestQueryParamater
        assert(result === RequestQueryParamater(Some(Page(1)), Some(Limit(10)), Some(Order.DESC)))
      }

      "convert to RequestQueryParamater with default value if key value is invalid" in {
        val result = Map(("page" -> "invalid"), ("limit" -> "invalid"), ("order" -> "invalid")).asRequestQueryParamater
        assert(result === RequestQueryParamater(Some(Page(1)), Some(Limit(10)), Some(Order.DESC)))
      }

      "convert to RequestQueryParamater if order param is uppercase" in {
        val result = Map(("order" -> "ASC")).asRequestQueryParamater
        assert(result === RequestQueryParamater(Some(Page(1)), Some(Limit(10)), Some(Order.ASC)))
      }

    }

  }

}
