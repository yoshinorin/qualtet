package net.yoshinorin.qualtet.domains.articles

import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.syntax.*
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.contents.Path

// testOnly net.yoshinorin.qualtet.domains.models.articles.ArticleSpec
class ArticleSpec extends AnyWordSpec {

  val responseArticle1: ResponseArticle = ResponseArticle(
    id = ContentId(generateUlid()),
    path = Path("/test"),
    title = "title",
    content = "this is a content",
    publishedAt = 0,
    updatedAt = 0
  )

  val responseArticle2: ResponseArticle = ResponseArticle(
    id = ContentId(generateUlid()),
    path = Path("/test/path2"),
    title = "title2",
    content = "this is a content2",
    publishedAt = 1,
    updatedAt = 2
  )

  val responseArticleWithLongLengthContent: ResponseArticle = ResponseArticle(
    id = ContentId(generateUlid()),
    path = Path("/test"),
    title = "title",
    content = "a" * 101,
    publishedAt = 0,
    updatedAt = 0
  )

  "ResponseArticle" should {
    "truncate content" in {
      assert(responseArticleWithLongLengthContent.content.length < 100)
    }

    // NOTE: content length are random. Can't test.
    "as JSON" in {
      responseArticle1.asJson
    }

    // NOTE: content length are random. Can't test.
    "as JSON Array" in {
      Seq(responseArticle1, responseArticle2).asJson
    }

  }

}
