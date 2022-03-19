package net.yoshinorin.qualtet.cache

import com.github.benmanes.caffeine.cache.{Cache => CaffeineCache}

class CacheModule[T1, T2](caffeineCache: CaffeineCache[T1, T2]) {

  def get(k: T1): Option[T2] = {
    Option(caffeineCache.getIfPresent(k))
  }

  def put(k: T1, v: T2): Unit = {
    caffeineCache.put(k, v)
  }

  def put(k: T1, v: Option[T2]): Unit = {
    v match {
      case Some(x: T2) => caffeineCache.put(k, x)
      case _ => // Nothing to do
    }
  }

  def invalidate(): Unit = {
    caffeineCache.invalidateAll()
  }

}
