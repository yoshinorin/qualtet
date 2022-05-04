package net.yoshinorin.qualtet.domains.sitemaps

import doobie.ConnectionIO

trait SitemapsRepository {

  def get: ConnectionIO[Seq[Url]]

}
