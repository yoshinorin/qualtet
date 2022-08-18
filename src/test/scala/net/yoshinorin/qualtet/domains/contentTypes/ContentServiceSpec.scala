package net.yoshinorin.qualtet.domains.contentTypes

import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.fixture.Fixture._

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.contentTypes.ContentTypeServiceSpec
class ContentTypeServiceSpec extends AnyWordSpec {

  "invalidate" should {
    "be callable" in {
      assert(contentTypeService.invalidate().unsafeRunSync() === ())
    }
  }

}
