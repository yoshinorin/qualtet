package net.yoshinorin.qualtet.domains.sitemaps

import doobie.ConnectionIO

object SitemapsRepository {

  def dispatch[T](request: SitemapRepositoryRequest[T]): ConnectionIO[T] = request match {
    case Get() => SitemapsQuery.get.to[Seq]
  }

}
