package net.yoshinorin.qualtet.cache

import cats.Monad

trait CacheRepository[F[_]: Monad, T1, T2] {

  def get(k: T1): F[Option[T2]]

  def put(k: T1, v: T2): F[Unit]

  def put(k: T1, v: Option[T2]): F[Unit]

  def invalidate(): F[Unit]

}

object CacheRepository {

  import com.github.benmanes.caffeine.cache.Cache as CaffeineCache

  given InMemoryCache[F[_]: Monad, T1, T2](using caffeineCache: CaffeineCache[T1, T2]): CacheRepository[F, T1, T2] = {

    new CacheRepository[F, T1, T2] {
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
  }

}
