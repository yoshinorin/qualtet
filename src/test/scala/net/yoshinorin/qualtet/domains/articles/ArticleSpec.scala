package net.yoshinorin.qualtet.domains.articles

import net.yoshinorin.qualtet.fixture.unsafe
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.syntax.*
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.contents.{ContentId, ContentPath}

// testOnly net.yoshinorin.qualtet.domains.articles.ArticleSpec
class ArticleSpec extends AnyWordSpec {

  val responseArticle1: ArticleResponseModel = ArticleResponseModel(
    id = ContentId(generateUlid()),
    path = ContentPath("/test").unsafe,
    title = "title",
    content = "this is a content",
    publishedAt = 0,
    updatedAt = 0
  )

  val responseArticle2: ArticleResponseModel = ArticleResponseModel(
    id = ContentId(generateUlid()),
    path = ContentPath("/test/path2").unsafe,
    title = "title2",
    content = "this is a content2",
    publishedAt = 1,
    updatedAt = 2
  )

  val responseArticleWithLongLengthContent: ArticleResponseModel = ArticleResponseModel(
    id = ContentId(generateUlid()),
    path = ContentPath("/test").unsafe,
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
