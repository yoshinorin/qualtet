package net.yoshinorin.qualtet.domains.models.sitemaps

import doobie.ConnectionIO
import doobie.implicits._

class DoobieSitemapsRepository extends SitemapsRepository {

  def get: ConnectionIO[Seq[Url]] = {
    sql"""
      SELECT path AS loc, updated_at AS lastmod
      FROM contents
      INNER JOIN robots ON
        contents.id = robots.content_id
      WHERE
        robots.attributes NOT LIKE '%noindex%'
      ORDER BY updated_at DESC
    """
      .query[Url]
      .to[Seq]
  }

}
