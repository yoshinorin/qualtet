package net.yoshinorin.qualtet.domains.models.article

import net.yoshinorin.qualtet.domains.models.articles.ResponseArticle
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.models.article.ArticleSpec
class ArticleSpec extends AnyWordSpec {

  "ResponseArticle" should {

    "truncate content" in {

      val responseArticle = ResponseArticle(
        path = "/test",
        title = "title",
        content = "a" * 101,
        publishedAt = 0,
        updatedAt = 0
      )

      assert(responseArticle.content.length < 100)
    }

  }

}
