package net.yoshinorin.qualtet.domains.models.articles

import doobie.ConnectionIO
import net.yoshinorin.qualtet.http.QueryParamatersAliases

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
   * @param sqlParams sql paramaters for limit, offset
   * @return Articles with ConnectionIO
   */
  def get(contentTypeId: String, sqlParams: QueryParamatersAliases.SqlParams): ConnectionIO[Seq[ResponseArticle]]
}
