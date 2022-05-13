package net.yoshinorin.qualtet.domains.articles

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.articles.RepositoryReqiests._

trait ArticleRepository {

  /**
   * get all Articles
   *
   * @param request GetWithCount case class
   * @return Articles & it's count with ConnectionIO
   */
  def dispatch(request: GetWithCount): ConnectionIO[Seq[(Int, ResponseArticle)]]

  /**
   * get Articles by TagId
   *
   * @param contentTypeId contentTypeId
   * @param tagId tagId
   * @param sqlParams sql parameters for limit, offset
   * @return Articles & it's count with ConnectionIO
   */
  // def findByTagIdWithCount(contentTypeId: ContentTypeId, tagId: TagId, sqlParams: SqlParams): ConnectionIO[Seq[(Int, ResponseArticle)]]

  /**
   * get Articles by TagName
   *
   * @param request FindByTagNameWithCount case class
   * @return Articles & it's count with ConnectionIO
   */
  def dispatch(request: FindByTagNameWithCount): ConnectionIO[Seq[(Int, ResponseArticle)]]
}
