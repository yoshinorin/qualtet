package net.yoshinorin.qualtet.domains.search

import doobie.Read
import doobie.implicits.*
import doobie.util.query.Query0

object SearchQuery {
  // https://tpolecat.github.io/doobie/docs/08-Fragments.html
  @SuppressWarnings(Array("org.wartremover.warts.IterableOps"))
  def search(query: List[String])(implicit
    responseSearchWithCount: Read[(Int, ResponseSearch)]
  ): Query0[(Int, ResponseSearch)] = {
    // TODO: LIMIT should be configurable
    // TODO: ORDER BY asc, title...etc
    // TODO: AND, OR
    // TODO: configurable REGEXP_REPLACE
    // TODO: exclude relative path in the site
    val likeQuerySub = query.map(q => fr"raw_content LIKE ${"%" + q + "%"}").reduce((a, b) => a ++ fr" AND " ++ b)
    val likeQuery = query.map(q => fr"filterd_contents.raw_content LIKE ${"%" + q + "%"}").reduce((a, b) => a ++ fr" AND " ++ b)
    sql"""
      SELECT
        count(1) OVER () AS count,
        path,
        title,
        raw_content AS content,
        published_at,
        updated_at
      FROM
        (
          SELECT
            path,
            title,
            REGEXP_REPLACE(raw_content, 'https?:.*?(?=\s)', '') as raw_content,
            published_at,
            updated_at
          FROM
            contents
          WHERE
            ${likeQuerySub}
        ) AS filterd_contents
      WHERE
        ${likeQuery}
      ORDER BY
        published_at DESC
      LIMIT 30
    """
      .query[(Int, ResponseSearch)]
  }
}
