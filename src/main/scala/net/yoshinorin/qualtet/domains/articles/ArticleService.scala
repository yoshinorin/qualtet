package net.yoshinorin.qualtet.domains.articles

import cats.effect.IO
import net.yoshinorin.qualtet.domains.ServiceBase
import net.yoshinorin.qualtet.domains.ServiceLogic._
import net.yoshinorin.qualtet.domains.{ServiceLogic, Continue, Done}
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeId, ContentTypeService}
import net.yoshinorin.qualtet.domains.feeds.ResponseFeed
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

class ArticleService(contentTypeService: ContentTypeService)(doobieContext: DoobieContextBase) extends ServiceBase {

  def get[A](
    data: A,
    queryParam: ArticlesQueryParameter
  )(f: (ContentTypeId, A, ArticlesQueryParameter) => ServiceLogic[Seq[(Int, ResponseArticle)]]): IO[ResponseArticleWithCount] = {
    for {
      c <- findBy("article", NotFound(s"content-type not found: article"))(contentTypeService.findByName)
      articlesWithCount <- transact(f(c.id, data, queryParam))(doobieContext)
    } yield
      if (articlesWithCount.nonEmpty) {
        ResponseArticleWithCount(articlesWithCount.map(_._1).head, articlesWithCount.map(_._2))
      } else {
        throw NotFound("articles not found")
      }
  }

  def getWithCount(queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {

    def execute(
      contentTypeId: ContentTypeId,
      none: Unit = (),
      queryParams: ArticlesQueryParameter
    ): ServiceLogic[Seq[(Int, ResponseArticle)]] = {
      val request = GetWithCount(contentTypeId, none, queryParams)
      val resultHandler: Seq[(Int, ResponseArticle)] => ServiceLogic[Seq[(Int, ResponseArticle)]] = (resultHandler: Seq[(Int, ResponseArticle)]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    this.get((), queryParam)(execute)
  }

  /*
  def getByTagIdWithCount(tagId: TagId, queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {
    this.get(tagId, queryParam)(articleRepository.findByTagIdWithCount)
  }
   */

  def getByTagNameWithCount(tagName: TagName, queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {

    def execute(
      contentTypeId: ContentTypeId,
      tagName: TagName,
      queryParams: ArticlesQueryParameter
    ): ServiceLogic[Seq[(Int, ResponseArticle)]] = {
      val request = FindByTagNameWithCount(contentTypeId, tagName, queryParams)
      val resultHandler: Seq[(Int, ResponseArticle)] => ServiceLogic[Seq[(Int, ResponseArticle)]] = (resultHandler: Seq[(Int, ResponseArticle)]) => {
        Done(resultHandler)
      }
      Continue(request, resultHandler)
    }

    this.get(tagName, queryParam)(execute)
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
