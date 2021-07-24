package net.yoshinorin.qualtet.domains.models.articles

import doobie.ConnectionIO
import net.yoshinorin.qualtet.http.ArticlesQueryParamater

trait ArticleRepository {

  /**
   * get all Articles
   *
   * @return Articles with ConnectionIO
   * TODO: order by
   * TODO: pagination
   * TODO: rename
   */
  def get(contentTypeId: String, queryParam: ArticlesQueryParamater): ConnectionIO[Seq[ResponseArticle]]
}
