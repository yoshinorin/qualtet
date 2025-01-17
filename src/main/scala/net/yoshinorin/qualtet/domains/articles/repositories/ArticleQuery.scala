package net.yoshinorin.qualtet.domains.articles

import doobie.Read
import doobie.implicits.*
import doobie.util.query.Query0
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.domains.series.SeriesName
import net.yoshinorin.qualtet.http.QueryParametersAliases.SqlParams
import net.yoshinorin.qualtet.http.Order
import doobie.util.fragment.Fragment

object ArticleQuery {

  // NOTE: can not build collect query if I use `fr"published_at ${sqlParams.order.value}"`.
  private def generageOrderByFragments(order: Order): Fragment = {
    order match {
      case Order.ASC => fr"published_at ASC"
      case _ => fr"published_at DESC"
    }
  }

  def getWithCount(contentTypeId: ContentTypeId, sqlParams: SqlParams): Read[(Int, ArticleReadModel)] ?=> Query0[(Int, ArticleReadModel)] = {
    val orderFrgments = generageOrderByFragments(sqlParams.order)

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
        ${orderFrgments}
      LIMIT
        ${sqlParams.limit}
      OFFSET
        ${sqlParams.offset}
    """
      .query[(Int, ArticleReadModel)]
  }

  def findByTagNameWithCount(
    contentTypeId: ContentTypeId,
    tagName: TagName,
    sqlParams: SqlParams
  ): Read[(Int, ArticleReadModel)] ?=> Query0[(Int, ArticleReadModel)] = {
    val orderFrgments = generageOrderByFragments(sqlParams.order)
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
        ${orderFrgments}
      LIMIT
        ${sqlParams.limit}
      OFFSET
        ${sqlParams.offset}
    """
      .query[(Int, ArticleReadModel)]
  }

  def findBySeriesNameWithCount(contentTypeId: ContentTypeId, seriesName: SeriesName): Read[(Int, ArticleReadModel)] ?=> Query0[(Int, ArticleReadModel)] = {
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
      .query[(Int, ArticleReadModel)]
  }
}
