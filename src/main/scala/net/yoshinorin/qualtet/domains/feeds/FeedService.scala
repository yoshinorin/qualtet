package net.yoshinorin.qualtet.domains.feeds

import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.{FeedsPagination, PaginationOps, PaginationRequestModel}
import net.yoshinorin.qualtet.domains.articles.ArticleWithCountResponseModel
import net.yoshinorin.qualtet.domains.Cacheable

class FeedService[F[_]: Monad](
  feedsPagination: PaginationOps[FeedsPagination],
  cache: CacheModule[IO, String, ArticleWithCountResponseModel],
  articleService: ArticleService[F]
) extends Cacheable[IO] {

  private val CACHE_KEY = "FEED_FULL_CACHE"

  def get(p: PaginationRequestModel): IO[Seq[FeedResponseModel]] = {

    def fromDb(): IO[ArticleWithCountResponseModel] = {
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
      articles <- maybeArticles match {
        case Some(a: ArticleWithCountResponseModel) => IO.pure(a)
        case _ =>
          for {
            dbArticles <- fromDb()
            _ <- cache.put(CACHE_KEY, dbArticles)
          } yield dbArticles
      }
    } yield toFeed(articles)
  }

  def invalidate(): IO[Unit] = {
    cache.invalidate()
  }

}
