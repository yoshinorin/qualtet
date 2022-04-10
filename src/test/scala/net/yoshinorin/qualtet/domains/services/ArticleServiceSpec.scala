package net.yoshinorin.qualtet.domains.services

import net.yoshinorin.qualtet.fixture.Fixture._
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.models.authors.AuthorName
import net.yoshinorin.qualtet.domains.models.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.models.robots.Attributes
import net.yoshinorin.qualtet.domains.models.tags.TagName

// testOnly net.yoshinorin.qualtet.domains.services.ArticleServiceSpec
class ArticleServiceSpec extends AnyWordSpec {

  val requestContents: List[RequestContent] = {
    (1 until 30).toList.map(i =>
      RequestContent(
        contentType = "article",
        path = Path(s"/test/path-${i}"),
        title = s"this is a title ${i}",
        rawContent = s"this is a raw content ${i}",
        htmlContent = s"this is a html content ${i}",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = Option(List(s"testTag${i}")),
        externalResources = Option(List())
      )
    )
  }

  val sameTagNameRequestContents: List[RequestContent] = {
    (1 until 15).toList.map(i =>
      RequestContent(
        contentType = "article",
        path = Path(s"/test/same/tags/${i}"),
        title = s"this is a same tag title ${i}",
        rawContent = s"this is a same tag raw content ${i}",
        htmlContent = s"this is a html content ${i}",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = Option(List("SameTag")),
        externalResources = Option(List())
      )
    )
  }

  // NOTE: create content and related data for test
  requestContents.foreach { rc => contentService.createContentFromRequest(AuthorName(author.name.value), rc).unsafeRunSync() }
  sameTagNameRequestContents.foreach { rc => contentService.createContentFromRequest(AuthorName(author.name.value), rc).unsafeRunSync() }

  "ArticleService" should {

    "getWithCount return ResponseArticleWithCount instances" in {
      val result = articleService.getWithCount(ArticlesQueryParameter(1, 5)).unsafeRunSync()
      assert(result.count > result.articles.size)
      assert(result.articles.size == 5)
      assert(result.articles == result.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))

      val result2 = articleService.getWithCount(ArticlesQueryParameter(1, 3)).unsafeRunSync()
      assert(result.count > result.articles.size)
      assert(result2.articles.size == 3)
      assert(result2.articles == result2.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))
    }

    "getByTagNameWithCount return ResponseArticleWithCount instances" in {
      val result = articleService.getByTagNameWithCount(TagName("SameTag"), ArticlesQueryParameter(1, 5)).unsafeRunSync()
      assert(result.count > result.articles.size)
      assert(result.articles.size == 5)
      assert(result.articles == result.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))

      result.articles.map(a => a.path.value).foreach { p => assert(p.startsWith("/test/same/tags/")) }
    }

    "getFeeds return ResponseFeed instances" in {
      val result = articleService.getFeeds(ArticlesQueryParameter(1, 5)).unsafeRunSync()
      assert(result.size == 5)
      assert(result == result.sortWith((x, y) => x.published > y.published))
    }

  }

}
