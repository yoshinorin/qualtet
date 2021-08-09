package net.yoshinorin.qualtet.domains.models.article

import io.circe.syntax._
import net.yoshinorin.qualtet.domains.models.articles.ResponseArticle
import net.yoshinorin.qualtet.domains.models.contents.Path
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.models.article.ArticleSpec
class ArticleSpec extends AnyWordSpec {

  "ResponseArticle" should {
    "truncate content" in {
      val responseArticle = ResponseArticle(
        path = Path("/test"),
        title = "title",
        content = "a" * 101,
        publishedAt = 0,
        updatedAt = 0
      )
      assert(responseArticle.content.length < 100)
    }

    // NOTE: content length are random. Can't test.
    "as JSON" in {
      ResponseArticle(
        path = Path("/test"),
        title = "title",
        content = "this is a content",
        publishedAt = 0,
        updatedAt = 0
      ).asJson
    }

    // NOTE: content length are random. Can't test.
    "as JSON Array" in {
      Seq(
        ResponseArticle(
          path = Path("/test/path1"),
          title = "title1",
          content = "this is a content1",
          publishedAt = 0,
          updatedAt = 0
        ),
        ResponseArticle(
          path = Path("/test/path2"),
          title = "title2",
          content = "this is a content2",
          publishedAt = 1,
          updatedAt = 2
        )
      ).asJson
    }

  }

}
