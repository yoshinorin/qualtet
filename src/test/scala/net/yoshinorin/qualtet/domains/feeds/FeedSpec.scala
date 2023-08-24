package net.yoshinorin.qualtet.domains.feeds

import net.yoshinorin.qualtet.domains.contents.Path
import net.yoshinorin.qualtet.syntax.*
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.feeds.FeedSpec
class FeedSpec extends AnyWordSpec {

  val feed1: ResponseFeed = ResponseFeed(
    title = "feedTitle1",
    link = Path("/feed1"),
    id = Path("/feed1"),
    published = 0,
    updated = 0
  )

  val feed2: ResponseFeed = ResponseFeed(
    title = "feedTitle2",
    link = Path("/feed2"),
    id = Path("/feed2"),
    published = 0,
    updated = 0
  )

  "ResponseFeed" should {

    "be convert as JSON" in {
      feed1.asJson
    }

    "be convert as JSON Array" in {
      Seq(feed1, feed2).asJson
    }

  }

}
