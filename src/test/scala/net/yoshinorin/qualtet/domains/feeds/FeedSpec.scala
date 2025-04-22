package net.yoshinorin.qualtet.domains.feeds

import net.yoshinorin.qualtet.domains.contents.ContentPath
import net.yoshinorin.qualtet.syntax.*
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.feeds.FeedSpec
class FeedSpec extends AnyWordSpec {

  val feed1: FeedResponseModel = FeedResponseModel(
    title = "feedTitle1",
    link = ContentPath("/feed1"),
    id = ContentPath("/feed1"),
    published = 0,
    updated = 0
  )

  val feed2: FeedResponseModel = FeedResponseModel(
    title = "feedTitle2",
    link = ContentPath("/feed2"),
    id = ContentPath("/feed2"),
    published = 0,
    updated = 0
  )

  "ResponseFeed" should {

    "convert as JSON" in {
      feed1.asJson
    }

    "convert as JSON Array" in {
      Seq(feed1, feed2).asJson
    }

  }

}
