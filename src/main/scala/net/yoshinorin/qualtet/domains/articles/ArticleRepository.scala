package net.yoshinorin.qualtet.domains.articles

import doobie.ConnectionIO

object ArticleRepository {

  def dispatch[T](request: ArticleRepositoryRequest[T]): ConnectionIO[T] = request match {
    case GetWithCount(contentTypeId, none, sqlParams) => ArticleQuery.getWithCount(contentTypeId, none, sqlParams).to[Seq]
    case FindByTagNameWithCount(contentTypeId, tagName, sqlParams) => ArticleQuery.findByTagNameWithCount(contentTypeId, tagName, sqlParams).to[Seq]
  }

}
