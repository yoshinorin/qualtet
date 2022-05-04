package net.yoshinorin.qualtet.domains.articles

import io.circe.syntax._
import net.yoshinorin.qualtet.fixture.Fixture._
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.models.articles.ArticleSpec
class ArticleSpec extends AnyWordSpec {

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
