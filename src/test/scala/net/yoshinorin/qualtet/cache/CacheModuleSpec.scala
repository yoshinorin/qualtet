package net.yoshinorin.qualtet.cache

import com.github.benmanes.caffeine.cache.{Cache => CaffeineCache, Caffeine}
import net.yoshinorin.qualtet.domains.contentTypes.ContentType
import net.yoshinorin.qualtet.fixture.Fixture.{articleContentType, contentTypeId}
import org.scalatest.wordspec.AnyWordSpec

import java.util.concurrent.TimeUnit

// testOnly net.yoshinorin.qualtet.cache.CacheModuleSpec
class CacheModuleSpec extends AnyWordSpec {

  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, ContentType]

  val contentTypeCache = new CacheModule[String, ContentType](contentTypeCaffeinCache)
  contentTypeCache.put(articleContentType.name, articleContentType)

  val caffeinCache: CaffeineCache[Int, String] =
    Caffeine.newBuilder().expireAfterAccess(3, TimeUnit.SECONDS).build[Int, String]

  "Cache" should {
    "hit" in {
      assert(contentTypeCache.get(articleContentType.name).get.id === contentTypeId)
    }

    "miss" in {
      assert(contentTypeCache.get("miss").isEmpty)
    }

    "hit optional" in {
      val cache = new CacheModule[Int, String](caffeinCache)
      cache.put(1, Option("foo"))
      assert(cache.get(1).get === "foo")
    }

    "miss optional" in {
      val cache = new CacheModule[Int, String](caffeinCache)
      cache.put(2, None)
      assert(cache.get(2).isEmpty)
    }

    "miss after expire" in {
      val cache = new CacheModule[Int, String](caffeinCache)
      cache.put(3, "foo")
      assert(cache.get(1).get === "foo")
      Thread.sleep(4000)
      assert(cache.get(3).isEmpty)
    }

    "flush" in {
      val cache = new CacheModule[Int, String](caffeinCache)
      cache.put(4, "bar")
      cache.put(5, "hoge")
      assert(cache.get(4).get === "bar")
      assert(cache.get(5).get === "hoge")
      cache.invalidate()
      assert(cache.get(4).isEmpty)
      assert(cache.get(5).isEmpty)
    }
  }
}
