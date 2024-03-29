package net.yoshinorin.qualtet.domains.sitemaps

import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.fixture.Fixture.*

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.sitemaps.SitemapServiceSpec
class SitemapServiceSpec extends AnyWordSpec {

  "invalidate" should {
    "callable" in {
      assert(sitemapService.invalidate().unsafeRunSync() === ())
    }
  }

}
