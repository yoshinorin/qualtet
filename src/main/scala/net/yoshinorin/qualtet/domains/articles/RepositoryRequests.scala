package net.yoshinorin.qualtet.domains.articles

import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.http.QueryParametersAliases.SqlParams
import net.yoshinorin.qualtet.domains.repository.requests._

trait ArticleRepositoryRequest[T] extends RepositoryRequest[T]
final case class GetWithCount(contentTypeId: ContentTypeId, none: Unit, sqlParams: SqlParams) extends ArticleRepositoryRequest[Seq[(Int, ResponseArticle)]] {
  def dispatch = ArticleRepository.dispatch(this)
}
final case class FindByTagNameWithCount(contentTypeId: ContentTypeId, tagName: TagName, sqlParams: SqlParams)
    extends ArticleRepositoryRequest[Seq[(Int, ResponseArticle)]] {
  def dispatch = ArticleRepository.dispatch(this)
}
