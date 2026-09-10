package net.yoshinorin.qualtet.domains.articles

import cats.data.ContT
import cats.Monad
import cats.implicits.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeId, ContentTypeName, ContentTypeService}
import net.yoshinorin.qualtet.domains.errors.{ArticleNotFound, ContentTypeNotFound, DomainError}
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.domains.series.{SeriesName, SeriesPath}
import net.yoshinorin.qualtet.domains.pagination.Pagination
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.domains.tags.TagPath

import scala.annotation.nowarn

class ArticleService[F[_]: Monad, G[_]: Monad @nowarn](
  articleRepositoryAdapter: ArticleRepositoryAdapter[G],
  contentTypeService: ContentTypeService[F, G]
)(using executer: Executer[F, G], loggerFactory: Log4CatsLoggerFactory[F]) {

  private given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  def get[A](
    data: A = (),
    pagination: Pagination
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
              executer.transact(f(c.id, data, pagination)).flatMap { articlesWithCount =>
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

  def getWithCount(p: Pagination): F[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(pagination = p)(articleRepositoryAdapter.getWithCount)
  }

  def getByTagNameWithCount(tagName: TagName, p: Pagination): F[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(tagName, p)(articleRepositoryAdapter.findByTagNameWithCount)
  }

  def getByTagPathWithCount(tagPath: TagPath, p: Pagination): F[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(tagPath, p)(articleRepositoryAdapter.findByTagPathWithCount)
  }

  def getBySeriesName(seriesName: SeriesName, p: Pagination): F[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(seriesName, p: Pagination)(articleRepositoryAdapter.findBySeriesNameWithCount)
  }

  def getBySeriesPath(seriesPath: SeriesPath, p: Pagination): F[Either[DomainError, ArticleWithCountResponseModel]] = {
    this.get(seriesPath, p: Pagination)(articleRepositoryAdapter.findBySeriesPathWithCount)
  }

}
