package net.yoshinorin.qualtet.domains.feeds

import net.yoshinorin.qualtet.domains.pagination.{Limit, Page, PaginationQueryParametersModel}
import net.yoshinorin.qualtet.fixture.Fixture.*

import cats.effect.IO
import cats.implicits.*
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.feeds.FeedServiceSpec
class FeedServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContentRequestModels(30, "feeds").unsafeCreateConternt()
  }

  private val pagination = feedsPaginationOps.make(PaginationQueryParametersModel(Option(Page(1)), Option(Limit(5)), None))

  "getFeeds return ResponseFeed instances" in {
    (for {
      _ <- net.yoshinorin.qualtet.fixture.Fixture.feedService.invalidate()
      feed <- net.yoshinorin.qualtet.fixture.Fixture.feedService.get(pagination).flatMap(_.liftTo[IO])
    } yield {
      assert(feed.size === 5)
      assert(feed === feed.sortWith((x, y) => x.published > y.published))
    }).unsafeRunSync()
  }

  "invalidate cache" in {
    assert(net.yoshinorin.qualtet.fixture.Fixture.feedService.invalidate().unsafeRunSync() === ())
  }

}
