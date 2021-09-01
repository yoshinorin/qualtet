package net.yoshinorin.qualtet.domains.models.sitemaps

import doobie.ConnectionIO

trait SitemapsRepository {

  def get: ConnectionIO[Seq[Url]]

}
