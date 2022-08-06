package net.yoshinorin.qualtet.domains

trait Cacheable {
  def invalidate(): Unit
}
