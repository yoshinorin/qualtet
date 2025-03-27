package net.yoshinorin.qualtet.domains.articles

import cats.effect.IO
import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.domains.contents.ContentRequestModel
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.domains.{ArticlesPagination, Limit, Page, PaginationOps, PaginationRequestModel}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.articles.ArticleServiceSpec
class ArticleServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContentRequestModels(30, "articles").unsafeCreateConternt()

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
    sameTagNameRequestContents.unsafeCreateConternt()
  }

  "ArticleService" should {

    "getWithCount return ResponseArticleWithCount instances" in {
      (for {
        pagination1 <- IO(PaginationRequestModel(Option(Page(1)), Option(Limit(5)), None))
        response1 <- articleService.getWithCount(pagination1)

        pagination2 <- IO(PaginationRequestModel(Option(Page(1)), Option(Limit(3)), None))
        response2 <- articleService.getWithCount(pagination2)
      } yield {
        // pagination1 assertion
        assert(response1.count > response1.articles.size)
        assert(response1.articles.size === 5)
        assert(response1.articles === response1.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))

        // pagination2 assertion
        assert(response1.count > response1.articles.size)
        assert(response2.articles.size === 3)
        assert(response2.articles === response2.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))
      }).unsafeRunSync()
    }

    "getWithCount with pagination instance returns ResponseArticleWithCount instances" in {
      val articlePagination = summon[PaginationOps[ArticlesPagination]]

      (for {
        pagination1 <- IO(PaginationRequestModel(Option(Page(1)), Option(Limit(5)), None))
        response1 <- articleService.getWithCount(articlePagination.make(pagination1))
        pagination2 <- IO(PaginationRequestModel(Option(Page(1)), Option(Limit(3)), None))
        response2 <- articleService.getWithCount(articlePagination.make(pagination2))
      } yield {
        // pagination1 assertion
        assert(response1.count > response1.articles.size)
        assert(response1.articles.size === 5)
        assert(response1.articles === response1.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))

        // pagination2 assertion
        assert(response1.count > response1.articles.size)
        assert(response2.articles.size === 3)
        assert(response2.articles === response2.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))
      }).unsafeRunSync()
    }

    "getByTagNameWithCount return ResponseArticleWithCount instances" in {
      (for {
        pagination <- IO(PaginationRequestModel(Option(Page(1)), Option(Limit(5)), None))
        response <- articleService.getByTagNameWithCount(TagName("SameTag"), pagination)
      } yield {
        assert(response.count > response.articles.size)
        assert(response.articles.size === 5)
        assert(response.articles === response.articles.sortWith((x, y) => x.publishedAt > y.publishedAt))

        response.articles.map(a => a.path.value).foreach { p =>
          assert(p.startsWith("/test/same/tags/"))
        }
      }).unsafeRunSync()
    }

  }

}
