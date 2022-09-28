package net.yoshinorin.qualtet.domains.feeds

import net.yoshinorin.qualtet.syntax._
import net.yoshinorin.qualtet.fixture.Fixture._
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.feeds.FeedSpec
class FeedSpec extends AnyWordSpec {

  "ResponseFeed" should {

    "be convert as JSON" in {
      feed1.asJson
    }

    "be convert as JSON Array" in {
      Seq(feed1, feed2).asJson
    }

  }

}
