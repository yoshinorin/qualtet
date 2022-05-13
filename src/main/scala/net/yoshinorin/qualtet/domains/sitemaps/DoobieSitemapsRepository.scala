package net.yoshinorin.qualtet.domains.sitemaps

import doobie.ConnectionIO
import RepositoryRequests.Get

class DoobieSitemapsRepository extends SitemapsRepository {

  override def dispatch(requests: Get): ConnectionIO[Seq[Url]] = {
    DoobieSitemapsQuery.get.to[Seq]
  }

}
