package net.yoshinorin.qualtet.domains.sitemaps

import doobie.ConnectionIO

trait SitemapsRepository[M[_]] {
  def get(): M[Seq[Url]]
}

class DoobieSitemapsRepository extends SitemapsRepository[ConnectionIO] {
  override def get(): ConnectionIO[Seq[Url]] = {
    SitemapsQuery.get.to[Seq]
  }
}
