package net.yoshinorin.qualtet.cache

import net.yoshinorin.qualtet.fixture.Fixture.cacheService
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.cache.CacheServiceModuleSpec
class CacheServiceModuleSpec extends AnyWordSpec {

  "CacheService" should {
    "be invalidateAll callable" in {
      assert(cacheService.invalidateAll().unsafeRunSync() === ())
    }
  }

}
