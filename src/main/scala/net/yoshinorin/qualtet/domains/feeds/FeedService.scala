package net.yoshinorin.qualtet.domains.feeds

import cats.effect.IO
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.domains.articles.ResponseArticleWithCount
import net.yoshinorin.qualtet.domains.Cacheable

class FeedService(cache: CacheModule[String, ResponseArticleWithCount], articleService: ArticleService) extends Cacheable {

  private val cacheKey = "feed-full-cache"

  def get(queryParam: ArticlesQueryParameter): IO[Seq[ResponseFeed]] = {

    def fromDb(): IO[ResponseArticleWithCount] = {
      for {
        x <- articleService.getWithCount(queryParam)
      } yield {
        cache.put(cacheKey, x)
        x
      }
    }

    def toFeed(ra: ResponseArticleWithCount): Seq[ResponseFeed] = {
      ra.articles.map(a => {
        ResponseFeed(
          title = a.title,
          link = a.path,
          id = a.path,
          published = a.publishedAt,
          updated = a.updatedAt
        )
      })
    }

    cache.get(cacheKey) match {
      case Some(x: ResponseArticleWithCount) => IO(toFeed(x))
      case _ =>
        for {
          articles <- fromDb()
        } yield toFeed(articles)
    }
  }

  def invalidate(): Unit = {
    cache.invalidate()
  }

}
