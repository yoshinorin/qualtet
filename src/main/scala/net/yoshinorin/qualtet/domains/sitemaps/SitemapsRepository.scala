package net.yoshinorin.qualtet.domains.sitemaps

import doobie.ConnectionIO
import RepositoryRequests.Get

object SitemapsRepository {

  def dispatch(request: Get): ConnectionIO[Seq[Url]] = {
    SitemapsQuery.get.to[Seq]
  }

}
