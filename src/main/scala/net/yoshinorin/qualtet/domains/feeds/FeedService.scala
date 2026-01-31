package net.yoshinorin.qualtet.domains.feeds

import cats.effect.IO
import cats.Monad
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.{FeedsPagination, PaginationOps, PaginationRequestModel}
import net.yoshinorin.qualtet.domains.articles.ArticleWithCountResponseModel
import net.yoshinorin.qualtet.domains.Cacheable
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.syntax.*

class FeedService[F[_]: Monad](
  feedsPagination: PaginationOps[FeedsPagination],
  cache: CacheModule[IO, String, ArticleWithCountResponseModel],
  articleService: ArticleService[F]
)(using loggerFactory: Log4CatsLoggerFactory[IO])
    extends Cacheable[IO] {

  private given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)
  private val CACHE_KEY = "FEED_FULL_CACHE"

  def get(p: PaginationRequestModel): IO[Either[DomainError, Seq[FeedResponseModel]]] = {

    def fromDb(): IO[Either[DomainError, ArticleWithCountResponseModel]] = {
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
        case Some(a: ArticleWithCountResponseModel) => IO.pure(Right(toFeed(a)))
        case _ =>
          fromDb().flatMap {
            case Right(dbArticles) =>
              cache.put(CACHE_KEY, dbArticles).map(_ => Right(toFeed(dbArticles)))
            case Left(error) =>
              Left(error).logLeft[IO](Error)
          }
      }
    } yield result
  }

  def invalidate(): IO[Unit] = {
    cache.invalidate()
  }

}
