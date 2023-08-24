package net.yoshinorin.qualtet.domains.feeds

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.http.ArticlesQueryParameter

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.FeedServiceSpec
class FeedServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  val requestContents = makeRequestContents(30, "feeds")

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContents(requestContents)
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
