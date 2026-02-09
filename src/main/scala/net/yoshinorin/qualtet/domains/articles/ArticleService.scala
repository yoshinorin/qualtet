package net.yoshinorin.qualtet.domains.articles

import cats.data.ContT
import cats.Monad
import cats.implicits.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeId, ContentTypeName, ContentTypeService}
import net.yoshinorin.qualtet.domains.errors.{ArticleNotFound, ContentTypeNotFound, DomainError}
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.domains.series.{SeriesName, SeriesPath}
import net.yoshinorin.qualtet.domains.{ArticlesPagination, Limit, Order, Page, Pagination, PaginationOps, PaginationRequestModel, TagsPagination}
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.domains.tags.TagPath

import scala.annotation.nowarn

class ArticleService[F[_]: Monad, G[_]: Monad](
  articleRepositoryAdapter: ArticleRepositoryAdapter[G],
  articlesPagination: PaginationOps[ArticlesPagination],
  tagsPagination: PaginationOps[TagsPagination],
  contentTypeService: ContentTypeService[F, G]
)(using executer: Executer[F, G], loggerFactory: Log4CatsLoggerFactory[F]) {

  private given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  def get[A](
    data: A = (),
    queryParam: Pagination
  )(
    f: (ContentTypeId, A, Pagination) => ContT[G, Seq[(Int, ArticleResponseModel)], Seq[(Int, ArticleResponseModel)]]
  ): F[Either[DomainError, ArticleWithCountResponseModel]] = {
    ContentTypeName("article") match {
      case Left(error) => Left(error).logLeft[F](Error)
      case Right(contentTypeName) =>
        for {
          maybeContentType <- contentTypeService.findByName(contentTypeName)
          result <- maybeContentType match {
            case Some(c) =>
              executer.transact(f(c.id, data, queryParam)).flatMap { articlesWithCount =>
                if (articlesWithCount.nonEmpty) {
                  Monad[F].pure(Right(ArticleWithCountResponseModel(articlesWithCount.map(_._1).headOption.getOrElse(0), articlesWithCount.map(_._2))))
                } else {
                  Left(ArticleNotFound(detail = "articles not found")).logLeft[F](Warn)
                }
              }
            case None =>
              Left(ContentTypeNotFound(detail = "content-type not found: article")).logLeft[F](Error)
          }
        } yield result
    }
  }

  def getWithCount(p: PaginationRequestModel): F[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(queryParam = articlesPagination.make(p))(articleRepositoryAdapter.getWithCount)
  }

  def getWithCount(p: Pagination): F[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(queryParam = p)(articleRepositoryAdapter.getWithCount)
  }

  def getByTagNameWithCount(tagName: TagName, p: PaginationRequestModel): F[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(tagName, tagsPagination.make(p))(articleRepositoryAdapter.findByTagNameWithCount)
  }

  def getByTagPathWithCount(tagPath: TagPath, p: PaginationRequestModel): F[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(tagPath, tagsPagination.make(p))(articleRepositoryAdapter.findByTagPathWithCount)
  }

  def getBySeriesName(seriesName: SeriesName): F[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(seriesName, articlesPagination.make(Page(0), Limit(100), Order.DESC))(articleRepositoryAdapter.findBySeriesNameWithCount)
  }

  def getBySeriesPath(seriesPath: SeriesPath): F[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(seriesPath, articlesPagination.make(Page(0), Limit(100), Order.DESC))(articleRepositoryAdapter.findBySeriesPathWithCount)
  }

}
