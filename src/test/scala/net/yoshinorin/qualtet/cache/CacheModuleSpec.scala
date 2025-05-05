package net.yoshinorin.qualtet.cache

import cats.effect.IO
import com.github.benmanes.caffeine.cache.{Cache as CaffeineCache, Caffeine}
import net.yoshinorin.qualtet.domains.contentTypes.ContentType
import net.yoshinorin.qualtet.fixture.Fixture.{articleContentType, contentTypeId}
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global
import java.util.concurrent.TimeUnit

// testOnly net.yoshinorin.qualtet.cache.CacheModuleSpec
class CacheModuleSpec extends AnyWordSpec {

  val contentTypeCaffeinCache: CaffeineCache[String, ContentType] =
    Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build[String, ContentType]

  val contentTypeCache = new CacheModule[IO, String, ContentType](contentTypeCaffeinCache)
  contentTypeCache.put(articleContentType.name, articleContentType)

  val caffeinCache: CaffeineCache[Int, String] =
    Caffeine.newBuilder().expireAfterAccess(3, TimeUnit.SECONDS).build[Int, String]

  "Cache" should {
    "hit" in {
      (for {
        cached <- contentTypeCache.get(articleContentType.name)
      } yield {
        assert(cached.get.id === contentTypeId)
      }).unsafeRunSync()
    }

    "miss" in {
      (for {
        cached <- contentTypeCache.get("miss")
      } yield {
        assert(cached.isEmpty)
      }).unsafeRunSync()
    }

    "hit optional" in {
      val cache = new CacheModule[IO, Int, String](caffeinCache)
      (for {
        _ <- cache.put(1, Option("foo"))
        cached <- cache.get(1)
      } yield {
        assert(cached === Some("foo"))
      }).unsafeRunSync()
    }

    "miss optional" in {
      val cache = new CacheModule[IO, Int, String](caffeinCache)
      (for {
        _ <- cache.put(2, None)
        cached <- cache.get(2)
      } yield {
        assert(cached.isEmpty)
      }).unsafeRunSync()
    }

    "miss after expire" in {
      val cache = new CacheModule[IO, Int, String](caffeinCache)
      (for {
        _ <- cache.put(3, "foo")
        cached <- cache.get(1)
        _ <- IO(Thread.sleep(4000))
        missed <- cache.get(3)
      } yield {
        assert(cached === Some("foo"))
        assert(missed.isEmpty)
      }).unsafeRunSync()
    }

    "flush" in {
      val cache = new CacheModule[IO, Int, String](caffeinCache)
      (for {
        _ <- cache.put(4, "bar")
        _ <- cache.put(5, "baz")
        cachedBar <- cache.get(4)
        cachedBaz <- cache.get(5)
        _ <- cache.invalidate()
        missedBar <- cache.get(4)
        missedBaz <- cache.get(5)
      } yield {
        assert(cachedBar === Some("bar"))
        assert(cachedBaz === Some("baz"))
        assert(missedBar.isEmpty)
        assert(missedBaz.isEmpty)
      }).unsafeRunSync()
    }
  }
}
