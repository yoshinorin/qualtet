package net.yoshinorin.qualtet.domains.models.articles

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.models.tags.TagName
import net.yoshinorin.qualtet.http.QueryParametersAliases.SqlParams

class DoobieArticleRepository extends ArticleRepository {

  // TOOD: delete none argument. Maybe lift is effective.
  def getWithCount(contentTypeId: ContentTypeId, none: Unit = (), sqlParams: SqlParams): ConnectionIO[Seq[(Int, ResponseArticle)]] = {
    DoobieArticleQuery.getWithCount(contentTypeId, none, sqlParams).to[Seq]
  }

  /*
  def findByTagIdWithCount(contentTypeId: ContentTypeId, tagId: TagId, sqlParams: SqlParams): ConnectionIO[Seq[(Int, ResponseArticle)]] = {
    DoobieArticleQuery.findByTagIdWithCount(contentTypeId, tagId, sqlParams).to[Seq]
  }
   */

  def findByTagNameWithCount(contentTypeId: ContentTypeId, tagName: TagName, sqlParams: SqlParams): ConnectionIO[Seq[(Int, ResponseArticle)]] = {
    DoobieArticleQuery.findByTagNameWithCount(contentTypeId, tagName, sqlParams).to[Seq]
  }
}
