package net.yoshinorin.qualtet.cache

import cats.Monad

trait CacheRepository[F[_]: Monad, T1, T2] {

  def get(k: T1): F[Option[T2]]

  def put(k: T1, v: T2): F[Unit]

  def put(k: T1, v: Option[T2]): F[Unit]

  def invalidate(): F[Unit]

}
