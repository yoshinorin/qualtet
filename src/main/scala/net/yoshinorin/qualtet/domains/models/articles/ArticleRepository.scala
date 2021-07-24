package net.yoshinorin.qualtet.domains.models.articles

import doobie.ConnectionIO
import net.yoshinorin.qualtet.http.QueryParametersAliases.SqlParams

trait ArticleRepository {

  /**
   * get number of articles
   *
   * @param contentTypeId contentTypeId
   * @return Number of articles with ConnectionIO
   */
  def count(contentTypeId: String): ConnectionIO[Int]

  /**
   * get all Articles
   *
   * @param contentTypeId contentTypeId
   * @param sqlParams sql parameters for limit, offset
   * @return Articles with ConnectionIO
   */
  def get(contentTypeId: String, sqlParams: SqlParams): ConnectionIO[Seq[ResponseArticle]]
}
