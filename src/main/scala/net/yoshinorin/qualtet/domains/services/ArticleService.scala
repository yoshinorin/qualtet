package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.Fail.NotFound
import net.yoshinorin.qualtet.domains.models.articles.{ArticleRepository, ResponseArticle, ResponseArticleWithCount}
import net.yoshinorin.qualtet.domains.models.contentTypes.{ContentType, ContentTypeId}
import net.yoshinorin.qualtet.domains.models.tags.TagId
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class ArticleService(
  articleRepository: ArticleRepository,
  contentTypeService: ContentTypeService
)(
  implicit doobieContext: DoobieContext
) {

  // TODO: move somewhere
  def contentType: IO[ContentType] = contentTypeService.findByName("article").flatMap {
    case None => IO.raiseError(NotFound(s"content-type not found: article"))
    case Some(x) => IO(x)
  }

  def get[A](
    data: A,
    queryParam: ArticlesQueryParameter
  )(f: (ContentTypeId, A, ArticlesQueryParameter) => ConnectionIO[Seq[(Int, ResponseArticle)]]): IO[ResponseArticleWithCount] =
    for {
      c <- this.contentType
      articlesWithCount <- f(c.id, data, queryParam).transact(doobieContext.transactor)
    } yield ResponseArticleWithCount(articlesWithCount.unzip._1.head, articlesWithCount.unzip._2)

  def getWithCount(queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {
    this.get((), queryParam)(articleRepository.getWithCount)
  }

  def getByTagIdWithCount(tagId: TagId, queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {
    this.get(tagId, queryParam)(articleRepository.findByTagIdWithCount)
  }

}
