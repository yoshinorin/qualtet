package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.Fail.NotFound
import net.yoshinorin.qualtet.domains.models.articles.{ArticleRepository, ResponseArticle, ResponseArticleWithCount}
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.models.feeds.ResponseFeed
import net.yoshinorin.qualtet.domains.models.tags.{TagId, TagName}
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

class ArticleService(
  articleRepository: ArticleRepository,
  contentTypeService: ContentTypeService
)(
  implicit doobieContext: DoobieContextBase
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

  def getByTagNameWithCount(tagName: TagName, queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {
    this.get(tagName, queryParam)(articleRepository.findByTagNameWithCount)
  }

  def getFeeds(queryParam: ArticlesQueryParameter): IO[Seq[ResponseFeed]] = {
    // TODO: Cache
    for {
      articles <- this.get((), queryParam)(articleRepository.getWithCount)
    } yield articles.articles.map(a => {
      new ResponseFeed(
        title = a.title,
        link = a.path,
        id = a.path,
        published = a.publishedAt,
        updated = a.updatedAt
      )
    })

  }

}
