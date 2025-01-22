package net.yoshinorin.qualtet.domains.feeds

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.domains.{ArticlesPagination, Limit, Page, Pagination, PaginationOps}

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.FeedServiceSpec
class FeedServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    val requestContents = makeRequestContents(30, "feeds")
    createContents(requestContents)
  }

  "getFeeds return ResponseFeed instances" in {
    val pagination = summon[PaginationOps[ArticlesPagination]]

    val result = (for {
      _ <- net.yoshinorin.qualtet.fixture.Fixture.feedService.invalidate()
      feed <- net.yoshinorin.qualtet.fixture.Fixture.feedService.get(pagination.make(Option(Page(1)), Option(Limit(5)), None))
    } yield feed).unsafeRunSync()

    assert(result.size === 5)
    assert(result === result.sortWith((x, y) => x.published > y.published))
  }

  "invalidate cache" in {
    assert(net.yoshinorin.qualtet.fixture.Fixture.feedService.invalidate().unsafeRunSync() === ())
  }

}
