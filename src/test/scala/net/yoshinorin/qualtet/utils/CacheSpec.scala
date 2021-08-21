package net.yoshinorin.qualtet.utils

import com.github.benmanes.caffeine.cache.{Caffeine, Cache => CaffeineCache}
import net.yoshinorin.qualtet.domains.models.contentTypes.{ContentType, ContentTypeId}
import org.scalatest.wordspec.AnyWordSpec

import java.util.concurrent.TimeUnit

// testOnly net.yoshinorin.qualtet.utils.CacheSpec
class CacheSpec extends AnyWordSpec {

  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, ContentType]

  val contentTypeCache = new Cache[String, ContentType](contentTypeCaffeinCache)
  val contentType: ContentType = ContentType(ContentTypeId("5214b4e2-485e-41b2-9e1f-996fc75bd879"), "article")
  contentTypeCache.put(contentType.name, contentType)

  val caffeinCache: CaffeineCache[Int, String] =
    Caffeine.newBuilder().expireAfterAccess(3, TimeUnit.SECONDS).build[Int, String]
  val cache = new Cache[Int, String](caffeinCache)

  "Cache" should {
    "hit" in {
      assert(contentTypeCache.get(contentType.name).get.id == ContentTypeId("5214b4e2-485e-41b2-9e1f-996fc75bd879"))
    }

    "miss" in {
      assert(contentTypeCache.get("miss").isEmpty)
    }

    "miss after expire" in {
      cache.put(1, "foo")
      assert(cache.get(1).get == "foo")
      Thread.sleep(4000)
      assert(cache.get(1).isEmpty)
    }

    "flush" in {
      cache.put(2, "bar")
      cache.put(3, "hoge")
      assert(cache.get(2).get == "bar")
      assert(cache.get(3).get == "hoge")
      cache.flush()
      assert(cache.get(2).isEmpty)
      assert(cache.get(3).isEmpty)
    }
  }
}
