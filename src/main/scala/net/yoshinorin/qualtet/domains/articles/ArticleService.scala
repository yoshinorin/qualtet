package net.yoshinorin.qualtet.domains.articles

import cats.effect.IO
import doobie.ConnectionIO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.articles.RepositoryReqiests._
import net.yoshinorin.qualtet.domains.ServiceBase
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeId, ContentTypeService}
import net.yoshinorin.qualtet.domains.feeds.ResponseFeed
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.domains.tags.TagName
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
  )(f: (ContentTypeId, A, ArticlesQueryParameter) => IO[Seq[(Int, ResponseArticle)]]): IO[ResponseArticleWithCount] = {
    for {
      c <- findBy("article", NotFound(s"content-type not found: article"))(contentTypeService.findByName)
      articlesWithCount <- f(c.id, data, queryParam)
    } yield
      if (articlesWithCount.nonEmpty) {
        ResponseArticleWithCount(articlesWithCount.map(_._1).head, articlesWithCount.map(_._2))
      } else {
        throw NotFound("articles not found")
      }
  }

  def getWithCount(queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {

    def makeRequest(
      contentTypeId: ContentTypeId,
      none: Unit,
      queryParams: ArticlesQueryParameter
    ): (GetWithCount, ConnectionIO[Seq[(Int, ResponseArticle)]] => ConnectionIO[Seq[(Int, ResponseArticle)]]) = {
      val request = GetWithCount(contentTypeId, none, queryParams)
      val resultHandler: ConnectionIO[Seq[(Int, ResponseArticle)]] => ConnectionIO[Seq[(Int, ResponseArticle)]] =
        (connectionIO: ConnectionIO[Seq[(Int, ResponseArticle)]]) => { connectionIO }
      (request, resultHandler)
    }

    def run(contentTypeId: ContentTypeId, none: Unit = (), queryParams: ArticlesQueryParameter): IO[Seq[(Int, ResponseArticle)]] = {
      val (request, _) = makeRequest(contentTypeId, none, queryParams)
      articleRepository.dispatch(request).transact(doobieContext.transactor)
    }

    this.get((), queryParam)(run)
  }

  /*
  def getByTagIdWithCount(tagId: TagId, queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {
    this.get(tagId, queryParam)(articleRepository.findByTagIdWithCount)
  }
   */

  def getByTagNameWithCount(tagName: TagName, queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {

    def makeRequest(
      contentTypeId: ContentTypeId,
      tagName: TagName,
      queryParams: ArticlesQueryParameter
    ): (FindByTagNameWithCount, ConnectionIO[Seq[(Int, ResponseArticle)]] => ConnectionIO[Seq[(Int, ResponseArticle)]]) = {
      val request = FindByTagNameWithCount(contentTypeId, tagName, queryParams)
      val resultHandler: ConnectionIO[Seq[(Int, ResponseArticle)]] => ConnectionIO[Seq[(Int, ResponseArticle)]] =
        (connectionIO: ConnectionIO[Seq[(Int, ResponseArticle)]]) => { connectionIO }
      (request, resultHandler)
    }

    def run(contentTypeId: ContentTypeId, tagName: TagName, queryParams: ArticlesQueryParameter): IO[Seq[(Int, ResponseArticle)]] = {
      val (request, _) = makeRequest(contentTypeId, tagName, queryParams)
      articleRepository.dispatch(request).transact(doobieContext.transactor)
    }

    this.get(tagName, queryParam)(run)
  }

  def getFeeds(queryParam: ArticlesQueryParameter): IO[Seq[ResponseFeed]] = {
    // TODO: Cache
    for {
      articles <- this.getWithCount(queryParam)
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
