package net.yoshinorin.qualtet.domains.feeds

import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.{FeedsPagination, PaginationOps, PaginationRequestModel}
import net.yoshinorin.qualtet.domains.articles.ArticleWithCountResponseModel
import net.yoshinorin.qualtet.domains.Cacheable
import net.yoshinorin.qualtet.domains.errors.DomainError

class FeedService[F[_]: Monad](
  feedsPagination: PaginationOps[FeedsPagination],
  cache: CacheModule[IO, String, ArticleWithCountResponseModel],
  articleService: ArticleService[F]
) extends Cacheable[IO] {

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
              IO.pure(Left(error))
          }
      }
    } yield result
  }

  def invalidate(): IO[Unit] = {
    cache.invalidate()
  }

}
