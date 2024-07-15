package net.yoshinorin.qualtet.domains.articles

import cats.data.ContT
import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeId, ContentTypeService}
import net.yoshinorin.qualtet.domains.errors.NotFound
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.domains.series.SeriesName
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

class ArticleService[F[_]: Monad](
  articleRepository: ArticleRepository[F],
  contentTypeService: ContentTypeService[F]
)(using executer: Executer[F, IO]) {

  def actions(
    contentTypeId: ContentTypeId,
    none: Unit = (),
    queryParams: ArticlesQueryParameter
  ): ContT[F, Seq[(Int, ResponseArticle)], Seq[(Int, ResponseArticle)]] = {
    ContT.apply[F, Seq[(Int, ResponseArticle)], Seq[(Int, ResponseArticle)]] { next =>
      articleRepository.getWithCount(contentTypeId, queryParams)
    }
  }

  def tagActions(
    contentTypeId: ContentTypeId,
    tagName: TagName,
    queryParams: ArticlesQueryParameter
  ): ContT[F, Seq[(Int, ResponseArticle)], Seq[(Int, ResponseArticle)]] = {
    ContT.apply[F, Seq[(Int, ResponseArticle)], Seq[(Int, ResponseArticle)]] { next =>
      articleRepository.findByTagNameWithCount(contentTypeId, tagName, queryParams)
    }
  }

  def seriesActions(
    contentTypeId: ContentTypeId,
    seriesName: SeriesName,
    queryParams: ArticlesQueryParameter // TODO: `Optional`
  ): ContT[F, Seq[(Int, ResponseArticle)], Seq[(Int, ResponseArticle)]] = {
    ContT.apply[F, Seq[(Int, ResponseArticle)], Seq[(Int, ResponseArticle)]] { next =>
      articleRepository.findBySeriesNameWithCount(contentTypeId, seriesName)
    }
  }

  def get[A](
    data: A = (),
    queryParam: ArticlesQueryParameter
  )(f: (ContentTypeId, A, ArticlesQueryParameter) => ContT[F, Seq[(Int, ResponseArticle)], Seq[(Int, ResponseArticle)]]): IO[ResponseArticleWithCount] = {
    for {
      c <- contentTypeService.findByName("article").throwIfNone(NotFound(detail = "content-type not found: article"))
      articlesWithCount <- executer.transact(f(c.id, data, queryParam))
    } yield
      if (articlesWithCount.nonEmpty) {
        ResponseArticleWithCount(articlesWithCount.map(_._1).headOption.getOrElse(0), articlesWithCount.map(_._2))
      } else {
        throw NotFound(detail = "articles not found")
      }
  }

  def getWithCount(queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {
    this.get(queryParam = queryParam)(actions)
  }

  def getByTagNameWithCount(tagName: TagName, queryParam: ArticlesQueryParameter): IO[ResponseArticleWithCount] = {
    this.get(tagName, queryParam)(tagActions)
  }

  def getBySeriesName(seriesName: SeriesName): IO[ResponseArticleWithCount] = {
    this.get(seriesName, ArticlesQueryParameter(0, 100))(seriesActions)
  }

}
