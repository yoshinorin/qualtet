package net.yoshinorin.qualtet.domains.feeds

import net.yoshinorin.qualtet.cache.CacheRepository
import net.yoshinorin.qualtet.domains.Cacheable
import net.yoshinorin.qualtet.domains.articles.{ArticleService, ArticleWithCountResponseModel}
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.domains.pagination.Pagination
import net.yoshinorin.qualtet.syntax.*

import cats.Monad
import cats.implicits.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}
import scala.annotation.nowarn

class FeedService[F[_]: Monad, G[_]: Monad @nowarn](
  cache: CacheRepository[F, String, ArticleWithCountResponseModel],
  articleService: ArticleService[F, G]
)(using loggerFactory: Log4CatsLoggerFactory[F])
    extends Cacheable[F] {

  private given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)
  private val CACHE_KEY = "FEED_FULL_CACHE"

  def get(pagination: Pagination): F[Either[DomainError, Seq[FeedResponseModel]]] = {

    def fromDb(): F[Either[DomainError, ArticleWithCountResponseModel]] = {
      articleService.getWithCount(pagination)
    }

    def toFeed(ra: ArticleWithCountResponseModel): Seq[FeedResponseModel] = {
      ra.articles
        .map(a => {
          FeedResponseModel(
            title = a.title,
            link = a.path,
            id = a.path,
            published = a.publishedAt,
            updated = a.updatedAt
          )
        })
    }

    for {
      maybeArticles <- cache.get(CACHE_KEY)
      result <- maybeArticles match {
        case Some(a: ArticleWithCountResponseModel) => Monad[F].pure(Right(toFeed(a)))
        case _ =>
          fromDb().flatMap {
            case Right(dbArticles) =>
              cache.put(CACHE_KEY, dbArticles).map(_ => Right(toFeed(dbArticles)))
            case Left(error) =>
              Left(error).logLeft[F](Error)
          }
      }
    } yield result
  }

  def invalidate(): F[Unit] = {
    cache.invalidate()
  }

}
