package net.yoshinorin.qualtet.syntax

import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.http.RequestQueryParamater

// testOnly net.yoshinorin.qualtet.syntax.HttpSpec
class HttpSpec extends AnyWordSpec {

  "http syntax" should {

    "asRequestQueryParamater" should {

      "be convert to RequestQueryParamater" in {
        val result = Map(("page" -> "3"), ("limit" -> "2")).asRequestQueryParamater
        assert(result === RequestQueryParamater(Some(3), Some(2)))
      }

      "be convert to RequestQueryParamater with default value if key is empty" in {
        val result = Map().asRequestQueryParamater
        assert(result === RequestQueryParamater(Some(1), Some(1)))
      }

    }

  }

}
