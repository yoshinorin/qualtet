package net.yoshinorin.qualtet.domains

import cats.effect.IO

trait Cacheable {
  def invalidate(): IO[Unit]
}
