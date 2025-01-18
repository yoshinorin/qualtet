package net.yoshinorin.qualtet.http

import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.http.QueryParameterSpec
class QueryParameterSpec extends AnyWordSpec {

  "QueryParameter" should {
    "default instance" in {
      val queryParameter = ArticlesQueryParameter()

      assert(queryParameter.page === 1)
      assert(queryParameter.limit === 10)
      assert(queryParameter.offset === 0)
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

    /* TODO:
    "limit is bigger than 10" in {
      val queryParameter = ArticlesQueryParameter(page = 1, limit = 11)

      assert(queryParameter.limit === 10)
    }

    "page is bigger than 1" in {
      val queryParameter = ArticlesQueryParameter(page = 5)

      assert(queryParameter.page === 5)
      assert(queryParameter.offset === 50)
    }
     */
  }

}
