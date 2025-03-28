package net.yoshinorin.qualtet.domains.feeds

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.domains.{Limit, Page, PaginationRequestModel}

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.feeds.FeedServiceSpec
class FeedServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContentRequestModels(30, "feeds").unsafeCreateConternt()
  }

  "getFeeds return ResponseFeed instances" in {
    (for {
      _ <- net.yoshinorin.qualtet.fixture.Fixture.feedService.invalidate()
      feed <- net.yoshinorin.qualtet.fixture.Fixture.feedService.get(PaginationRequestModel(Option(Page(1)), Option(Limit(5)), None))
    } yield {
      assert(feed.size === 5)
      assert(feed === feed.sortWith((x, y) => x.published > y.published))
    }).unsafeRunSync()
  }

  "invalidate cache" in {
    assert(net.yoshinorin.qualtet.fixture.Fixture.feedService.invalidate().unsafeRunSync() === ())
  }

}
