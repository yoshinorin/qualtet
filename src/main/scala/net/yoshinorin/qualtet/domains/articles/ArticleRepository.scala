package net.yoshinorin.qualtet.domains.articles

import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.http.QueryParametersAliases.SqlParams

trait ArticleRepository[M[_]] {
  def getWithCount(contentTypeId: ContentTypeId, sqlParams: SqlParams): M[Seq[(Int, ResponseArticle)]]
  def findByTagNameWithCount(contentTypeId: ContentTypeId, tagName: TagName, sqlParams: SqlParams): M[Seq[(Int, ResponseArticle)]]
}
