package net.yoshinorin.qualtet.domains.articles

import com.github.plokhotnyuk.jsoniter_scala.core._
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
      writeToArray(responseArticle1)
    }

    // NOTE: content length are random. Can't test.
    "as JSON Array" in {
      writeToArray(Seq(responseArticle1, responseArticle2))
    }

  }

}
