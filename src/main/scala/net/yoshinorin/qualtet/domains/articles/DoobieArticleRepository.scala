package net.yoshinorin.qualtet.domains.articles

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.articles.RepositoryReqiests._

class DoobieArticleRepository extends ArticleRepository {

  // TOOD: delete none argument. Maybe lift is effective.
  override def dispatch(request: GetWithCount): ConnectionIO[Seq[(Int, ResponseArticle)]] = {
    DoobieArticleQuery.getWithCount(request.contentTypeId, request.none, request.sqlParams).to[Seq]
  }

  /*
  def findByTagIdWithCount(contentTypeId: ContentTypeId, tagId: TagId, sqlParams: SqlParams): ConnectionIO[Seq[(Int, ResponseArticle)]] = {
    DoobieArticleQuery.findByTagIdWithCount(contentTypeId, tagId, sqlParams).to[Seq]
  }
   */

  override def dispatch(request: FindByTagNameWithCount): ConnectionIO[Seq[(Int, ResponseArticle)]] = {
    DoobieArticleQuery.findByTagNameWithCount(request.contentTypeId, request.tagName, request.sqlParams).to[Seq]
  }
}
