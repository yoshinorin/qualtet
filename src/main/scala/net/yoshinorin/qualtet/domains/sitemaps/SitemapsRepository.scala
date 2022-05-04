package net.yoshinorin.qualtet.domains.sitemaps

import doobie.ConnectionIO
import RepositoryRequests.Get

trait SitemapsRepository {

  def dispatch(requests: Get): ConnectionIO[Seq[Url]]

}
