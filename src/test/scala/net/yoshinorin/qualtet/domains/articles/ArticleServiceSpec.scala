package net.yoshinorin.qualtet.domains.articles

import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.domains.contents.ContentRequestModel
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.domains.{ArticlesPagination, Limit, Page, PaginationOps, PaginationRequestModel}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.ArticleServiceSpec
class ArticleServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    val requestContents = makeRequestContents(30, "articles")
    createContents(requestContents)

    val sameTagNameRequestContents: List[ContentRequestModel] = {
      (1 until 15).toList
        .map(_.toString())
        .map(i =>
          ContentRequestModel(
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
    createContents(sameTagNameRequestContents)
  }

  "ArticleService" should {

    "getWithCount return ResponseArticleWithCount instances" in {
      val pagination1 = PaginationRequestModel(Option(Page(1)), Option(Limit(5)), None)
      val result = articleService.getWithCount(pagination1).unsafeRunSync()
      assert(result.count > result.articles.size)
      assert(result.articles.size === 5)
      assert(result.articles === result.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))

      val pagination2 = PaginationRequestModel(Option(Page(1)), Option(Limit(3)), None)
      val result2 = articleService.getWithCount(pagination2).unsafeRunSync()
      assert(result.count > result.articles.size)
      assert(result2.articles.size === 3)
      assert(result2.articles === result2.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))
    }

    "getWithCount with pagination instance returns ResponseArticleWithCount instances" in {
      val articlePagination = summon[PaginationOps[ArticlesPagination]]
      val pagination1 = PaginationRequestModel(Option(Page(1)), Option(Limit(5)), None)
      val result = articleService.getWithCount(articlePagination.make(pagination1)).unsafeRunSync()
      assert(result.count > result.articles.size)
      assert(result.articles.size === 5)
      assert(result.articles === result.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))

      val pagination2 = PaginationRequestModel(Option(Page(1)), Option(Limit(3)), None)
      val result2 = articleService.getWithCount(articlePagination.make(pagination2)).unsafeRunSync()
      assert(result.count > result.articles.size)
      assert(result2.articles.size === 3)
      assert(result2.articles === result2.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))
    }

    "getByTagNameWithCount return ResponseArticleWithCount instances" in {
      val pagination = PaginationRequestModel(Option(Page(1)), Option(Limit(5)), None)
      val result = articleService.getByTagNameWithCount(TagName("SameTag"), pagination).unsafeRunSync()
      assert(result.count > result.articles.size)
      assert(result.articles.size === 5)
      assert(result.articles === result.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))

      result.articles.map(a => a.path.value).foreach { p => assert(p.startsWith("/test/same/tags/")) }
    }

  }

}
