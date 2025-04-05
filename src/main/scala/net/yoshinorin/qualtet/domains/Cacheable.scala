package net.yoshinorin.qualtet.domains

trait Cacheable[F[_]] {
  def invalidate(): F[Unit]
}
