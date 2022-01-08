package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.Fail.NotFound
import net.yoshinorin.qualtet.domains.models.articles.{ArticleRepository, ResponseArticle, ResponseArticleWithCount}
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.models.tags.TagId
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class ArticleService(
  articleRepository: ArticleRepository,
  contentTypeService: ContentTypeService
)(
  implicit doobieContext: DoobieContext
) extends ServiceBase {

  def get[A](
    data: A,
    queryParam: ArticlesQueryParameter
  )(f: (ContentTypeId, A, ArticlesQueryParameter) => ConnectionIO[Seq[(Int, ResponseArticle)]]): IO[ResponseArticleWithCount] =
    for {
      c <- findBy("article", NotFound(s"content-type not found: article"))(contentTypeService.findByName)
      articlesWithCount <- f(c.id, data, queryParam).transact(doobieContext.transactor)
    } yield ResponseArticleWithCount(articlesWithCount.unzip._1.head, articlesWithCount.unzip._2)

  def getWithCount(queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {
    this.get((), queryParam)(articleRepository.getWithCount)
  }

  def getByTagIdWithCount(tagId: TagId, queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {
    this.get(tagId, queryParam)(articleRepository.findByTagIdWithCount)
  }

}
