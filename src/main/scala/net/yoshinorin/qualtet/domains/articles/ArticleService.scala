package net.yoshinorin.qualtet.domains.articles

import cats.data.ContT
import cats.effect.IO
import cats.implicits.*
import cats.Monad
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeId, ContentTypeName, ContentTypeService}
import net.yoshinorin.qualtet.domains.errors.{ArticleNotFound, ContentTypeNotFound}
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.domains.series.{SeriesName, SeriesPath}
import net.yoshinorin.qualtet.domains.{ArticlesPagination, Limit, Order, Page, Pagination, PaginationOps, PaginationRequestModel, TagsPagination}
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.domains.tags.TagPath

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
      contentTypeName <- ContentTypeName("article").liftTo[IO]
      c <- contentTypeService.findByName(contentTypeName).errorIfNone(ContentTypeNotFound(detail = "content-type not found: article")).flatMap(_.liftTo[IO])
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

  def getByTagPathWithCount(tagPath: TagPath, p: PaginationRequestModel): IO[ArticleWithCountResponseModel] = {
    this.get(tagPath, tagsPagination.make(p))(articleRepositoryAdapter.findByTagPathWithCount)
  }

  def getBySeriesName(seriesName: SeriesName): IO[ArticleWithCountResponseModel] = {
    this.get(seriesName, articlesPagination.make(Page(0), Limit(100), Order.DESC))(articleRepositoryAdapter.findBySeriesNameWithCount)
  }

  def getBySeriesPath(seriesPath: SeriesPath): IO[ArticleWithCountResponseModel] = {
    this.get(seriesPath, articlesPagination.make(Page(0), Limit(100), Order.DESC))(articleRepositoryAdapter.findBySeriesPathWithCount)
  }

}
