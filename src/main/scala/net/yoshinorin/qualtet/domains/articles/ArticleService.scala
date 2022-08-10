package net.yoshinorin.qualtet.domains.articles

import cats.effect.IO
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue}
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeId, ContentTypeService}
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase
import net.yoshinorin.qualtet.syntax._
class ArticleService(contentTypeService: ContentTypeService)(doobieContext: DoobieContextBase) {

  def get[A](
    data: A = (),
    queryParam: ArticlesQueryParameter
  )(f: (ContentTypeId, A, ArticlesQueryParameter) => Action[Seq[(Int, ResponseArticle)]]): IO[ResponseArticleWithCount] = {
    for {
      c <- contentTypeService.findByName("article").throwIfNone(NotFound(s"content-type not found: article"))
      articlesWithCount <- f(c.id, data, queryParam).perform.andTransact(doobieContext)
    } yield
      if (articlesWithCount.nonEmpty) {
        ResponseArticleWithCount(articlesWithCount.map(_._1).headOption.getOrElse(0), articlesWithCount.map(_._2))
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
      Continue(GetWithCount(contentTypeId, queryParams), Action.buildNext[Seq[(Int, ResponseArticle)]])
    }

    this.get(queryParam = queryParam)(actions)
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
      Continue(FindByTagNameWithCount(contentTypeId, tagName, queryParams), Action.buildNext[Seq[(Int, ResponseArticle)]])
    }

    this.get(tagName, queryParam)(actions)
  }

}
