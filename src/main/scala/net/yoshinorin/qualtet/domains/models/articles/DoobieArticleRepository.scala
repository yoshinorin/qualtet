package net.yoshinorin.qualtet.domains.models.articles

import doobie.ConnectionIO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.models.tags.TagId
import net.yoshinorin.qualtet.http.QueryParametersAliases.SqlParams
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class DoobieArticleRepository(doobie: DoobieContext) extends ArticleRepository {

  import doobie.ctx._

  // TOOD: delete none argument. Maybe lift is effective.
  def getWithCount(contentTypeId: ContentTypeId, none: Unit = (), sqlParams: SqlParams): ConnectionIO[Seq[(Int, ResponseArticle)]] = {
    sql"""
      SELECT
        count(1) OVER () AS count,
        path,
        title,
        html_content,
        published_at,
        updated_at
      FROM
        contents
      WHERE
        content_type_id = $contentTypeId
      ORDER BY
        published_at DESC
      LIMIT
        ${sqlParams.limit}
      OFFSET
        ${sqlParams.offset}
    """
      .query[(Int, ResponseArticle)]
      .to[Seq]
  }

  def findByTagIdWithCount(contentTypeId: ContentTypeId, tagId: TagId, sqlParams: SqlParams): ConnectionIO[Seq[(Int, ResponseArticle)]] = {
    sql"""
      SELECT
        count(1) OVER () AS count,
        path,
        title,
        html_content,
        published_at,
        updated_at
      FROM
        contents
      LEFT JOIN contents_tagging ON
        contents.id = contents_tagging.content_id
      LEFT JOIN tags ON
        contents_tagging.tag_id = tags.id
      WHERE
        content_type_id = $contentTypeId
      AND
        tags.id = $tagId
      ORDER BY
        published_at DESC
      LIMIT
        ${sqlParams.limit}
      OFFSET
        ${sqlParams.offset}
    """
      .query[(Int, ResponseArticle)]
      .to[Seq]
  }
}
