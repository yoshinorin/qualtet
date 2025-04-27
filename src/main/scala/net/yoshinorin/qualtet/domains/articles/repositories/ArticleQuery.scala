package net.yoshinorin.qualtet.domains.articles

import doobie.Read
import doobie.syntax.all.*
import doobie.util.query.Query0
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.tags.{TagName, TagPath}
import net.yoshinorin.qualtet.domains.series.SeriesPath
import net.yoshinorin.qualtet.domains.{Order, Pagination}
import doobie.util.fragment.Fragment

object ArticleQuery {

  // NOTE: can not build collect query if I use `fr"published_at ${pagination.order.value}"`.
  private def generateOrderByFragments(order: Order): Fragment = {
    order match {
      case Order.ASC => fr"published_at ASC"
      // NOTE: This implementation will suffer from performance degradation as the number of records increases.
      case Order.RANDOM => fr"rand()"
      case _ => fr"published_at DESC"
    }
  }

  def getWithCount(contentTypeId: ContentTypeId, pagination: Pagination): Read[(Int, ArticleReadModel)] ?=> Query0[(Int, ArticleReadModel)] = {
    val orderFrgments = generateOrderByFragments(pagination.order)

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
        ${pagination.limit.toInt}
      OFFSET
        ${pagination.offset}
    """
      .query[(Int, ArticleReadModel)]
  }

  def findByTagNameWithCount(
    contentTypeId: ContentTypeId,
    tagName: TagName,
    pagination: Pagination
  ): Read[(Int, ArticleReadModel)] ?=> Query0[(Int, ArticleReadModel)] = {
    val orderFrgments = generateOrderByFragments(pagination.order)
    sql"""
      SELECT
        count(1) OVER () AS count,
        contents.id,
        contents.path,
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
        ${pagination.limit.toInt}
      OFFSET
        ${pagination.offset}
    """
      .query[(Int, ArticleReadModel)]
  }

  def findByTagPathWithCount(
    contentTypeId: ContentTypeId,
    tagPath: TagPath,
    pagination: Pagination
  ): Read[(Int, ArticleReadModel)] ?=> Query0[(Int, ArticleReadModel)] = {
    val orderFrgments = generateOrderByFragments(pagination.order)
    sql"""
      SELECT
        count(1) OVER () AS count,
        contents.id,
        contents.path,
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
        tags.path = ${tagPath.value}
      ORDER BY
        ${orderFrgments}
      LIMIT
        ${pagination.limit.toInt}
      OFFSET
        ${pagination.offset}
    """
      .query[(Int, ArticleReadModel)]
  }

  def findBySeriesPathWithCount(contentTypeId: ContentTypeId, seriesPath: SeriesPath): Read[(Int, ArticleReadModel)] ?=> Query0[(Int, ArticleReadModel)] = {
    sql"""
        SELECT
        count(1) OVER () AS count,
        contents.id,
        contents.path,
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
      	series.path = ${seriesPath.value}
      ORDER BY
        published_at ASC
    """
      .query[(Int, ArticleReadModel)]
  }
}
