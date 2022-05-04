package net.yoshinorin.qualtet.domains.articles

import doobie.implicits._
import doobie.util.query.Query0
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.http.QueryParametersAliases.SqlParams

object DoobieArticleQuery {

  // TOOD: delete none argument. Maybe lift is effective.
  def getWithCount(contentTypeId: ContentTypeId, none: Unit = (), sqlParams: SqlParams): Query0[(Int, ResponseArticle)] = {
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
  }

  /*
  def findByTagIdWithCount(contentTypeId: ContentTypeId, tagId: TagId, sqlParams: SqlParams): Query0[(Int, ResponseArticle)] = {
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
  }
   */

  def findByTagNameWithCount(contentTypeId: ContentTypeId, tagName: TagName, sqlParams: SqlParams): Query0[(Int, ResponseArticle)] = {
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
        tags.name = $tagName
      ORDER BY
        published_at DESC
      LIMIT
        ${sqlParams.limit}
      OFFSET
        ${sqlParams.offset}
    """
      .query[(Int, ResponseArticle)]
  }
}
