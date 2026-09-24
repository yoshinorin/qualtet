package net.yoshinorin.qualtet.syntax

import net.yoshinorin.qualtet.domains.pagination.{Limit, Order, Page, PaginationQueryParametersModel}
import net.yoshinorin.qualtet.domains.period.{From, Period, To}

import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.syntax.HttpSpec
class HttpSpec extends AnyWordSpec {

  "http syntax" should {

    "asPagination" should {

      "convert to Pagination" in {
        val result = Map(("page" -> "3"), ("limit" -> "2"), ("order" -> "asc")).asPagination
        assert(result === PaginationQueryParametersModel(Some(Page(3)), Some(Limit(2)), Some(Order.ASC)))
      }

      "convert to Pagination with default value if key is empty" in {
        val result = Map().asPagination
        assert(result === PaginationQueryParametersModel(Some(Page(1)), Some(Limit(10)), Some(Order.DESC)))
      }

      "convert to Pagination with default value if key value is invalid" in {
        val result = Map(("page" -> "invalid"), ("limit" -> "invalid"), ("order" -> "invalid")).asPagination
        assert(result === PaginationQueryParametersModel(Some(Page(1)), Some(Limit(10)), Some(Order.DESC)))
      }

      "convert to Pagination if order param is uppercase" in {
        val result = Map(("order" -> "ASC")).asPagination
        assert(result === PaginationQueryParametersModel(Some(Page(1)), Some(Limit(10)), Some(Order.ASC)))
      }

    }

    "asPeriod" should {

      "convert to Published if both bounds are given" in {
        val result = Map(("published_from" -> "100"), ("published_to" -> "200")).asPeriod
        assert(result === Seq(Period.Published(Some(From(100)), Some(To(200)))))
      }

      "convert to Updated if both bounds are given" in {
        val result = Map(("updated_from" -> "100"), ("updated_to" -> "200")).asPeriod
        assert(result === Seq(Period.Updated(Some(From(100)), Some(To(200)))))
      }

      "convert to Published if only `from` is given" in {
        val result = Map(("published_from" -> "100")).asPeriod
        assert(result === Seq(Period.Published(Some(From(100)), None)))
      }

      "convert to Published if only `to` is given" in {
        val result = Map(("published_to" -> "200")).asPeriod
        assert(result === Seq(Period.Published(None, Some(To(200)))))
      }

      "convert to Updated if only `from` is given" in {
        val result = Map(("updated_from" -> "100")).asPeriod
        assert(result === Seq(Period.Updated(Some(From(100)), None)))
      }

      "convert both targets at once" in {
        val result = Map(("published_from" -> "100"), ("updated_to" -> "200")).asPeriod
        assert(result === Seq(Period.Published(Some(From(100)), None), Period.Updated(None, Some(To(200)))))
      }

      "treat an empty value as not given" in {
        val result = Map(("published_from" -> "100"), ("published_to" -> "")).asPeriod
        assert(result === Seq(Period.Published(Some(From(100)), None)))
      }

      "treat a blank value as not given" in {
        val result = Map(("published_from" -> "  "), ("published_to" -> "200")).asPeriod
        assert(result === Seq(Period.Published(None, Some(To(200)))))
      }

      "be empty if no params are given" in {
        val result = Map[String, String]().asPeriod
        assert(result === Seq())
      }

      "be empty if both bounds are empty" in {
        val result = Map(("published_from" -> ""), ("published_to" -> "")).asPeriod
        assert(result === Seq())
      }

      "drop the target if the value is not a number" in {
        val result = Map(("published_from" -> "invalid"), ("published_to" -> "200")).asPeriod
        assert(result === Seq())
      }

      "drop only the invalid target" in {
        val result = Map(("published_from" -> "invalid"), ("updated_from" -> "100")).asPeriod
        assert(result === Seq(Period.Updated(Some(From(100)), None)))
      }

      "ignore unrelated params" in {
        val result = Map(("page" -> "3"), ("limit" -> "2")).asPeriod
        assert(result === Seq())
      }

    }

  }

}
