package net.yoshinorin.qualtet.domains.search

import doobie.Read
import doobie.implicits._
import doobie.util.query.Query0
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.http.QueryParametersAliases.SqlParams

object SearchQuery {
  // https://tpolecat.github.io/doobie/docs/08-Fragments.html
  def search(query: List[String])(implicit
    responseSearchWithCount: Read[(Int, ResponseSearch)]
  ): Query0[(Int, ResponseSearch)] = {
    // TODO: LIMIT should be configurable
    // TODO: ORDER BY asc, title...etc
    // TODO: AND, OR
    val qs = query.map(q => fr"raw_content LIKE ${"%" + q + "%"}").reduce((a, b) => a ++ fr" AND " ++ b)
    sql"""
      SELECT
        count(1) OVER () AS count,
        path,
        title,
        raw_content AS content,
        published_at,
        updated_at
      FROM
        contents
      WHERE
        ${qs}
      ORDER BY
        published_at DESC
      LIMIT 30
    """
      .query[(Int, ResponseSearch)]
  }
}