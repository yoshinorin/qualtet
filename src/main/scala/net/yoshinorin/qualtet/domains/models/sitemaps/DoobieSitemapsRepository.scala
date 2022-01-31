package net.yoshinorin.qualtet.domains.models.sitemaps

import doobie.ConnectionIO

class DoobieSitemapsRepository extends SitemapsRepository {

  def get: ConnectionIO[Seq[Url]] = {
    DoobieSitemapsQuery.get.to[Seq]
  }

}
