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
