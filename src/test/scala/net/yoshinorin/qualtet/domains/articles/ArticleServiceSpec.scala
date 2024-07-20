package net.yoshinorin.qualtet.domains.articles

import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.ArticleServiceSpec
class ArticleServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  val sameTagNameRequestContents: List[RequestContent] = {
    (1 until 15).toList
      .map(_.toString())
      .map(i =>
        RequestContent(
          contentType = "article",
          path = Path(s"/test/same/tags/${i}"),
          title = s"this is a same tag title ${i}",
          rawContent = s"this is a same tag raw content ${i}",
          htmlContent = s"this is a html content ${i}",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          tags = List("SameTag"),
          externalResources = List()
        )
      )
  }

  val requestContents = makeRequestContents(30, "articles")

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContents(requestContents)
    createContents(sameTagNameRequestContents)
  }

  "ArticleService" should {

    "getWithCount return ResponseArticleWithCount instances" in {
      val result = articleService.getWithCount(ArticlesQueryParameter(1, 5)).unsafeRunSync()
      assert(result.count > result.articles.size)
      assert(result.articles.size === 5)
      assert(result.articles === result.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))

      val result2 = articleService.getWithCount(ArticlesQueryParameter(1, 3)).unsafeRunSync()
      assert(result.count > result.articles.size)
      assert(result2.articles.size === 3)
      assert(result2.articles === result2.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))
    }

    "getByTagNameWithCount return ResponseArticleWithCount instances" in {
      val result = articleService.getByTagNameWithCount(TagName("SameTag"), ArticlesQueryParameter(1, 5)).unsafeRunSync()
      assert(result.count > result.articles.size)
      assert(result.articles.size === 5)
      assert(result.articles === result.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))

      result.articles.map(a => a.path.value).foreach { p => assert(p.startsWith("/test/same/tags/")) }
    }

  }

}
