package net.yoshinorin.qualtet.domains.feeds

import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.fixture.Fixture._
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.feeds.FeedSpec
class FeedSpec extends AnyWordSpec {

  "ResponseFeed" should {

    "be convert as JSON" in {
      new String(writeToArray(feed1))
    }

    "be convert as JSON Array" in {
      new String(writeToArray(Seq(feed1, feed2)))
    }

  }

}
