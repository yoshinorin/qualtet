package net.yoshinorin.qualtet.domains.articles

import cats.effect.IO
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue, Done}
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeId, ContentTypeService}
import net.yoshinorin.qualtet.domains.feeds.ResponseFeed
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase
import net.yoshinorin.qualtet.syntax._
class ArticleService(contentTypeService: ContentTypeService)(doobieContext: DoobieContextBase) {

  def get[A](
    data: A,
    queryParam: ArticlesQueryParameter
  )(f: (ContentTypeId, A, ArticlesQueryParameter) => Action[Seq[(Int, ResponseArticle)]]): IO[ResponseArticleWithCount] = {
    for {
      c <- contentTypeService.findByName("article").throwIfNone(NotFound(s"content-type not found: article"))
      articlesWithCount <- f(c.id, data, queryParam).perform.andTransact(doobieContext)
    } yield
      if (articlesWithCount.nonEmpty) {
        ResponseArticleWithCount(articlesWithCount.map(_._1).head, articlesWithCount.map(_._2))
      } else {
        throw NotFound("articles not found")
      }
  }

  def getWithCount(queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {

    def actions(
      contentTypeId: ContentTypeId,
      none: Unit = (),
      queryParams: ArticlesQueryParameter
    ): Action[Seq[(Int, ResponseArticle)]] = {
      val request = GetWithCount(contentTypeId, none, queryParams)
      val resultHandler: Seq[(Int, ResponseArticle)] => Action[Seq[(Int, ResponseArticle)]] = (resultHandler: Seq[(Int, ResponseArticle)]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    this.get((), queryParam)(actions)
  }

  /*
  def getByTagIdWithCount(tagId: TagId, queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {
    this.get(tagId, queryParam)(articleRepository.findByTagIdWithCount)
  }
   */

  def getByTagNameWithCount(tagName: TagName, queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {

    def actions(
      contentTypeId: ContentTypeId,
      tagName: TagName,
      queryParams: ArticlesQueryParameter
    ): Action[Seq[(Int, ResponseArticle)]] = {
      val request = FindByTagNameWithCount(contentTypeId, tagName, queryParams)
      val resultHandler: Seq[(Int, ResponseArticle)] => Action[Seq[(Int, ResponseArticle)]] = (resultHandler: Seq[(Int, ResponseArticle)]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    this.get(tagName, queryParam)(actions)
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
