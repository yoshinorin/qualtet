package net.yoshinorin.qualtet.domains.models.articles

import doobie.ConnectionIO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.http.QueryParametersAliases.SqlParams
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class DoobieArticleRepository(doobie: DoobieContext) extends ArticleRepository {

  import doobie.ctx._

  /**
   * get number of articles
   *
   * @param contentTypeId contentTypeId
   * @return Number of articles with ConnectionIO
   */
  def count(contentTypeId: ContentTypeId): ConnectionIO[Int] = {
    sql"""
      SELECT count(1)
      FROM contents
        WHERE content_type_id = $contentTypeId
    """
      .query[Int]
      .unique
  }

  def get(contentTypeId: ContentTypeId, sqlParams: SqlParams): ConnectionIO[Seq[ResponseArticle]] = {
    sql"""
      SELECT path, title, html_content, published_at, updated_at
      FROM contents
        WHERE content_type_id = $contentTypeId
        ORDER BY published_at desc
        LIMIT ${sqlParams.limit}
        OFFSET ${sqlParams.offset}
    """
      .query[ResponseArticle]
      .to[Seq]
  }
}
