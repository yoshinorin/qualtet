package net.yoshinorin.qualtet.domains.models.sitemaps

import doobie.ConnectionIO
import doobie.implicits._
import io.getquill.{idiom => _}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class DoobieSitemapsRepository(doobie: DoobieContext) extends SitemapsRepository {

  def get: ConnectionIO[Seq[Url]] = {
    // TODO: exclude noindex page
    sql"""
      SELECT path AS loc, updated_at AS lastmod
      FROM contents
      ORDER BY updated_at DESC
    """
      .query[Url]
      .to[Seq]
  }

}
