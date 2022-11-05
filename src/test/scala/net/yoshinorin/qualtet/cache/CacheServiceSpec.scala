package net.yoshinorin.qualtet.cache

import net.yoshinorin.qualtet.Modules._
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.cache.CacheServiceModuleSpec
class CacheServiceModuleSpec extends AnyWordSpec {

  "CacheService" should {
    "be invalidateAll callable" in {
      assert(cacheService.invalidateAll().unsafeRunSync() === ())
    }
  }

}
