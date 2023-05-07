package net.yoshinorin.qualtet.domains.feeds

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import net.yoshinorin.qualtet.fixture.Fixture._
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.Modules._

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.FeedServiceSpec
class FeedServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  val requestContents: List[RequestContent] = {
    (1 until 30).toList
      .map(_.toString())
      .map(_.toString())
      .map(i =>
        RequestContent(
          contentType = "article",
          path = Path(s"/test/feed-${i}"),
          title = s"this is a title for feed ${i}",
          rawContent = s"this is a raw content for feed ${i}",
          htmlContent = s"this is a html content for feed ${i}",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          tags = List(s"testTagFeed${i}"),
          externalResources = List()
        )
      )
  }

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    requestContents.foreach { rc => contentService.createContentFromRequest(AuthorName(author.name.value), rc).unsafeRunSync() }
  }

  "getFeeds return ResponseFeed instances" in {
    val result = (for {
      _ <- net.yoshinorin.qualtet.fixture.Fixture.feedService.invalidate()
      feed <- net.yoshinorin.qualtet.fixture.Fixture.feedService.get(ArticlesQueryParameter(1, 5))
    } yield feed).unsafeRunSync()

    assert(result.size === 5)
    assert(result === result.sortWith((x, y) => x.published > y.published))
  }

  "be invalidate cache" in {
    assert(net.yoshinorin.qualtet.fixture.Fixture.feedService.invalidate().unsafeRunSync() === ())
  }

}
