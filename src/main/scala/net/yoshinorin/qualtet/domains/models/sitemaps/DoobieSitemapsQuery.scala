package net.yoshinorin.qualtet.domains.models.sitemaps

import doobie.implicits.toSqlInterpolator
import doobie.util.query.Query0

object DoobieSitemapsQuery {

  def get: Query0[Url] = {
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
  }

}
