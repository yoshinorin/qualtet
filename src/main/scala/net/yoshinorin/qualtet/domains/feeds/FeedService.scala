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
  cache: CacheModule[String, ArticleWithCountResponseModel],
  articleService: ArticleService[F]
) extends Cacheable {

  private val cacheKey = "feed-full-cache"

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

    cache.get(cacheKey) match {
      case Some(x: ArticleWithCountResponseModel) => IO(toFeed(x))
      case _ =>
        for {
          articles <- fromDb()
        } yield {
          cache.put(cacheKey, articles)
          toFeed(articles)
        }
    }
  }

  def invalidate(): IO[Unit] = {
    IO(cache.invalidate())
  }

}
