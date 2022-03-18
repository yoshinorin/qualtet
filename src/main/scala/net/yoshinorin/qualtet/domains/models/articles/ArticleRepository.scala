package net.yoshinorin.qualtet.domains.models.articles

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.models.tags.{TagId, TagName}
import net.yoshinorin.qualtet.http.QueryParametersAliases.SqlParams

trait ArticleRepository {

  /**
   * get all Articles
   *
   * @param contentTypeId contentTypeId
   * @param none dummy data. Should delete this argument. Maybe lift is effective.
   * @param sqlParams sql parameters for limit, offset
   * @return Articles & it's count with ConnectionIO
   *
   * TODO: delete none argument.
   */
  def getWithCount(contentTypeId: ContentTypeId, none: Unit, sqlParams: SqlParams): ConnectionIO[Seq[(Int, ResponseArticle)]]

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
   * @param contentTypeId contentTypeId
   * @param tagName tagName
   * @param sqlParams sql parameters for limit, offset
   * @return Articles & it's count with ConnectionIO
   */
  def findByTagNameWithCount(contentTypeId: ContentTypeId, tagName: TagName, sqlParams: SqlParams): ConnectionIO[Seq[(Int, ResponseArticle)]]
}
