package net.yoshinorin.qualtet.domains.feeds

import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.fixture.Fixture._
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.http.ArticlesQueryParameter

// testOnly net.yoshinorin.qualtet.domains.FeedServiceSpec
class FeedServiceSpec extends AnyWordSpec {

  val requestContents: List[RequestContent] = {
    (1 until 30).toList.map(_.toString()).map(_.toString()).map(i =>
      RequestContent(
        contentType = "article",
        path = Path(s"/test/feed-${i}"),
        title = s"this is a title for feed ${i}",
        rawContent = s"this is a raw content for feed ${i}",
        htmlContent = s"this is a html content for feed ${i}",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = Option(List(s"testTagFeed${i}")),
        externalResources = Option(List())
      )
    )
  }

  // NOTE: create content and related data for test
  requestContents.foreach { rc => contentService.createContentFromRequest(AuthorName(author.name.value), rc).unsafeRunSync() }

  "getFeeds return ResponseFeed instances" in {
    val result = (for {
      _ <- feedService.invalidate()
      feed <- feedService.get(ArticlesQueryParameter(1, 5))
    } yield feed).unsafeRunSync()

    assert(result.size === 5)
    assert(result === result.sortWith((x, y) => x.published > y.published))
  }

  "be invalidate cache" in {
    assert(feedService.invalidate().unsafeRunSync() === ())
  }

}
