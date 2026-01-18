package net.yoshinorin.qualtet.domains.articles

import cats.data.ContT
import cats.effect.IO
import cats.implicits.*
import cats.Monad
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeId, ContentTypeName, ContentTypeService}
import net.yoshinorin.qualtet.domains.errors.{ArticleNotFound, ContentTypeNotFound, DomainError}
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
)(using executer: Executer[F, IO], loggerFactory: Log4CatsLoggerFactory[IO]) {

  private given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  def get[A](
    data: A = (),
    queryParam: Pagination
  )(
    f: (ContentTypeId, A, Pagination) => ContT[F, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]]
  ): IO[Either[DomainError, ArticleWithCountResponseModel]] = {
    ContentTypeName("article") match {
      case Left(error) => Left(error).logLeft[IO](Error)
      case Right(contentTypeName) =>
        for {
          maybeContentType <- contentTypeService.findByName(contentTypeName)
          result <- maybeContentType match {
            case Some(c) =>
              executer.transact(f(c.id, data, queryParam)).flatMap { articlesWithCount =>
                if (articlesWithCount.nonEmpty) {
                  IO.pure(Right(ArticleWithCountResponseModel(articlesWithCount.map(_._1).headOption.getOrElse(0), articlesWithCount.map(_._2))))
                } else {
                  Left(ArticleNotFound(detail = "articles not found")).logLeft[IO](Warn)
                }
              }
            case None =>
              Left(ContentTypeNotFound(detail = "content-type not found: article")).logLeft[IO](Error)
          }
        } yield result
    }
  }

  def getWithCount(p: PaginationRequestModel): IO[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(queryParam = articlesPagination.make(p))(articleRepositoryAdapter.getWithCount)
  }

  def getWithCount(p: Pagination): IO[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(queryParam = p)(articleRepositoryAdapter.getWithCount)
  }

  def getByTagNameWithCount(tagName: TagName, p: PaginationRequestModel): IO[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(tagName, tagsPagination.make(p))(articleRepositoryAdapter.findByTagNameWithCount)
  }

  def getByTagPathWithCount(tagPath: TagPath, p: PaginationRequestModel): IO[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(tagPath, tagsPagination.make(p))(articleRepositoryAdapter.findByTagPathWithCount)
  }

  def getBySeriesName(seriesName: SeriesName): IO[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(seriesName, articlesPagination.make(Page(0), Limit(100), Order.DESC))(articleRepositoryAdapter.findBySeriesNameWithCount)
  }

  def getBySeriesPath(seriesPath: SeriesPath): IO[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(seriesPath, articlesPagination.make(Page(0), Limit(100), Order.DESC))(articleRepositoryAdapter.findBySeriesPathWithCount)
  }

}
