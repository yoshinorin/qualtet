package net.yoshinorin.qualtet.domains.articles

import cats.data.ContT
import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.errors.{ArticleNotFound, ContentTypeNotFound}
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.domains.series.SeriesName
import net.yoshinorin.qualtet.domains.{ArticlesPagination, Limit, Order, Page, Pagination, PaginationOps, PaginationRequestModel, TagsPagination}
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

class ArticleService[F[_]: Monad](
  articleRepositoryAdapter: ArticleRepositoryAdapter[F],
  articlesPagination: PaginationOps[ArticlesPagination],
  tagsPagination: PaginationOps[TagsPagination],
  contentTypeService: ContentTypeService[F]
)(using executer: Executer[F, IO]) {

  def get[A](
    data: A = (),
    queryParam: Pagination
  )(
    f: (ContentTypeId, A, Pagination) => ContT[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]]
  ): IO[ArticleWithCountResponseModel] = {
    for {
      c <- contentTypeService.findByName("article").throwIfNone(ContentTypeNotFound(detail = "content-type not found: article"))
      articlesWithCount <- executer.transact(f(c.id, data, queryParam))
    } yield
      if (articlesWithCount.nonEmpty) {
        ArticleWithCountResponseModel(articlesWithCount.map(_._1).headOption.getOrElse(0), articlesWithCount.map(_._2))
      } else {
        throw ArticleNotFound(detail = "articles not found")
      }
  }

  def getWithCount(p: PaginationRequestModel): IO[ArticleWithCountResponseModel] = {
    this.get(queryParam = articlesPagination.make(p))(articleRepositoryAdapter.getWithCount)
  }

  def getWithCount(p: Pagination): IO[ArticleWithCountResponseModel] = {
    this.get(queryParam = p)(articleRepositoryAdapter.getWithCount)
  }

  def getByTagNameWithCount(tagName: TagName, p: PaginationRequestModel): IO[ArticleWithCountResponseModel] = {
    this.get(tagName, tagsPagination.make(p))(articleRepositoryAdapter.findByTagNameWithCount)
  }

  def getBySeriesName(seriesName: SeriesName): IO[ArticleWithCountResponseModel] = {
    this.get(seriesName, articlesPagination.make(Page(0), Limit(100), Order.DESC))(articleRepositoryAdapter.findBySeriesNameWithCount)
  }

}
