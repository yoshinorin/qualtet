package net.yoshinorin.qualtet.domains.articles

import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.actions.Action._
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeId, ContentTypeService}
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.domains.series.{SeriesName, ResponseSeries}
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.infrastructure.db.Transactor
import net.yoshinorin.qualtet.syntax._

class ArticleService[M[_]: Monad](
  articleRepository: ArticleRepository[M],
  contentTypeService: ContentTypeService[M]
)(using transactor: Transactor[M]) {

  def actions(
    contentTypeId: ContentTypeId,
    none: Unit = (),
    queryParams: ArticlesQueryParameter
  ): Action[Seq[(Int, ResponseArticle)]] = {
    Continue(articleRepository.getWithCount(contentTypeId, queryParams), Action.done[Seq[(Int, ResponseArticle)]])
  }

  def tagActions(
    contentTypeId: ContentTypeId,
    tagName: TagName,
    queryParams: ArticlesQueryParameter
  ): Action[Seq[(Int, ResponseArticle)]] = {
    Continue(
      articleRepository.findByTagNameWithCount(contentTypeId, tagName, queryParams),
      Action.done[Seq[(Int, ResponseArticle)]]
    )
  }

  def seriesActions(
    contentTypeId: ContentTypeId,
    seriesName: SeriesName,
    queryParams: ArticlesQueryParameter // TODO: `Optional`
  ): Action[Seq[(Int, ResponseArticle)]] = {
    Continue(
      articleRepository.findBySeriesNameWithCount(contentTypeId, seriesName),
      Action.done[Seq[(Int, ResponseArticle)]]
    )
  }

  def get[A](
    data: A = (),
    queryParam: ArticlesQueryParameter
  )(f: (ContentTypeId, A, ArticlesQueryParameter) => Action[Seq[(Int, ResponseArticle)]]): IO[ResponseArticleWithCount] = {
    for {
      c <- contentTypeService.findByName("article").throwIfNone(NotFound(s"content-type not found: article"))
      articlesWithCount <- transactor.transact(f(c.id, data, queryParam))
    } yield
      if (articlesWithCount.nonEmpty) {
        ResponseArticleWithCount(articlesWithCount.map(_._1).headOption.getOrElse(0), articlesWithCount.map(_._2))
      } else {
        throw NotFound("articles not found")
      }
  }

  def getWithCount(queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {
    this.get(queryParam = queryParam)(actions)
  }

  /*
  def getByTagIdWithCount(tagId: TagId, queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {
    this.get(tagId, queryParam)(articleRepository.findByTagIdWithCount)
  }
   */

  def getByTagNameWithCount(tagName: TagName, queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {
    this.get(tagName, queryParam)(tagActions)
  }

  def getBySeriesName(seriesName: SeriesName): IO[ResponseArticleWithCount] = {
    this.get(seriesName, ArticlesQueryParameter(0, 100))(seriesActions)
  }

}
