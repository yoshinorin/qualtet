package net.yoshinorin.qualtet.cache

import cats.Monad
import com.github.benmanes.caffeine.cache.Cache as CaffeineCache

class InMemoryCache[F[_]: Monad, T1, T2](caffeineCache: CaffeineCache[T1, T2]) extends CacheRepository[F, T1, T2] {

  override def get(k: T1): F[Option[T2]] = {
    Monad[F].pure(Option(caffeineCache.getIfPresent(k)))
  }

  override def put(k: T1, v: T2): F[Unit] = {
    Monad[F].pure(caffeineCache.put(k, v))
  }

  override def put(k: T1, v: Option[T2]): F[Unit] = {
    v match {
      case Some(x: T2) => Monad[F].pure(caffeineCache.put(k, x))
      case _ => Monad[F].pure(()) // Nothing to do
    }
  }

  override def invalidate(): F[Unit] = {
    Monad[F].pure(caffeineCache.invalidateAll())
  }

}
