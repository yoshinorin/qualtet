package net.yoshinorin.qualtet.domains.articles

import doobie.{Read, Write}
import doobie.implicits.*
import doobie.util.query.Query0
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.domains.series.SeriesName
import net.yoshinorin.qualtet.http.QueryParametersAliases.SqlParams

object ArticleQuery {

  def getWithCount(contentTypeId: ContentTypeId, sqlParams: SqlParams)(implicit
    responseArticlesWithCount: Read[(Int, ResponseArticle)]
  ): Query0[(Int, ResponseArticle)] = {
    sql"""
      SELECT
        count(1) OVER () AS count,
        id,
        path,
        title,
        html_content,
        published_at,
        updated_at
      FROM
        contents
      WHERE
        content_type_id = ${contentTypeId.value}
      ORDER BY
        published_at DESC
      LIMIT
        ${sqlParams.limit}
      OFFSET
        ${sqlParams.offset}
    """
      .query[(Int, ResponseArticle)]
  }

  def findByTagNameWithCount(contentTypeId: ContentTypeId, tagName: TagName, sqlParams: SqlParams)(implicit
    responseArticlesWithCount: Read[(Int, ResponseArticle)]
  ): Query0[(Int, ResponseArticle)] = {
    sql"""
      SELECT
        count(1) OVER () AS count,
        contents.id,
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
        content_type_id = ${contentTypeId.value}
      AND
        tags.name = ${tagName.value}
      ORDER BY
        published_at DESC
      LIMIT
        ${sqlParams.limit}
      OFFSET
        ${sqlParams.offset}
    """
      .query[(Int, ResponseArticle)]
  }

  def findBySeriesNameWithCount(contentTypeId: ContentTypeId, seriesName: SeriesName)(implicit
    responseArticlesWithCount: Read[(Int, ResponseArticle)]
  ): Query0[(Int, ResponseArticle)] = {
    sql"""
        SELECT
        count(1) OVER () AS count,
        contents.id,
        path,
        contents.title,
        html_content,
        published_at,
        updated_at
      FROM
        contents
      LEFT JOIN contents_serializing ON
        contents.id = contents_serializing.content_id
      LEFT JOIN series ON
        contents_serializing.series_id = series.id
      WHERE
        content_type_id = ${contentTypeId.value}
      AND
      	series.name = ${seriesName.value}
      ORDER BY
        published_at ASC
    """
      .query[(Int, ResponseArticle)]
  }
}
