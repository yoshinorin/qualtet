package net.yoshinorin.qualtet.domains.models.articles

import doobie.ConnectionIO

trait ArticleRepository {

  /**
   * get all Articles
   *
   * @return Articles with ConnectionIO
   * TODO: order by
   * TODO: pagination
   * TODO: rename
   */
  def get(contentTypeId: String): ConnectionIO[Seq[ResponseArticle]]

  /**
   * get all Articles
   *
   * @return Articles with ConnectionIO
   * TODO: order by
   * TODO: rename
   */
  def getAll(contentTypeId: String): ConnectionIO[Seq[ResponseArticleSimple]]
}
