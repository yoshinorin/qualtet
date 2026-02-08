package net.yoshinorin.qualtet.domains.feeds

import cats.Monad
import cats.implicits.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.{FeedsPagination, PaginationOps, PaginationRequestModel}
import net.yoshinorin.qualtet.domains.articles.ArticleWithCountResponseModel
import net.yoshinorin.qualtet.domains.Cacheable
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.syntax.*

import scala.annotation.nowarn

class FeedService[G[_]: Monad, F[_]: Monad](
  feedsPagination: PaginationOps[FeedsPagination],
  cache: CacheModule[F, String, ArticleWithCountResponseModel],
  articleService: ArticleService[G, F]
)(using loggerFactory: Log4CatsLoggerFactory[F])
    extends Cacheable[F] {

  private given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)
  private val CACHE_KEY = "FEED_FULL_CACHE"

  def get(p: PaginationRequestModel): F[Either[DomainError, Seq[FeedResponseModel]]] = {

    def fromDb(): F[Either[DomainError, ArticleWithCountResponseModel]] = {
      articleService.getWithCount(feedsPagination.make(p))
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
