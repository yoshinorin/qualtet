package net.yoshinorin.qualtet.domains.sitemaps

import doobie.Read
import doobie.syntax.all.toSqlInterpolator
import doobie.util.query.Query0

object SitemapsQuery {

  def get: Read[UrlReadModel] ?=> Query0[UrlReadModel] = {
    sql"""
      SELECT path AS loc, updated_at AS lastmod
      FROM contents
      INNER JOIN robots ON
        contents.id = robots.content_id
      WHERE
        robots.attributes NOT LIKE '%noindex%'
      ORDER BY updated_at DESC
    """
      .query[UrlReadModel]
  }

}
