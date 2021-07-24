package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.Fail.NotFound
import net.yoshinorin.qualtet.domains.models.articles.{ArticleRepository, ResponseArticle, ResponseArticleWithCount}
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentType
import net.yoshinorin.qualtet.http.ArticlesQueryParamater
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

  // TODO: get from cache
  def count(): IO[Int] = {
    for {
      c <- this.contentType // TODO: get from cache
      cnt <- articleRepository.count(c.id).transact(doobieContext.transactor)
    } yield cnt
  }

  def get(queryParam: ArticlesQueryParamater): IO[Seq[ResponseArticle]] = {
    for {
      c <- this.contentType // TODO: get from cache
      articles <- articleRepository.get(c.id, queryParam).transact(doobieContext.transactor)
    } yield articles.map(a => {
      // TODO: why apply when execute SQL with doobie
      ResponseArticle(
        a.path,
        a.title,
        a.content,
        a.publishedAt,
        a.updatedAt
      )
    })
  }

  def getWithCount(queryParam: ArticlesQueryParamater): IO[ResponseArticleWithCount] = {
    for {
      c <- this.count
      a <- this.get(queryParam)
    } yield ResponseArticleWithCount(c, a)
  }

}
