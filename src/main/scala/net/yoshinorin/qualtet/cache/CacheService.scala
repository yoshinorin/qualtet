package net.yoshinorin.qualtet.cache

import cats.Monad
import cats.effect.IO
import net.yoshinorin.qualtet.domains.sitemaps.SitemapService
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.domains.feeds.FeedService
import org.slf4j.LoggerFactory

class CacheService[M[_]: Monad](
  sitemapService: SitemapService[M],
  contentTypeService: ContentTypeService[M],
  feedService: FeedService[M]
) {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def invalidateAll(): IO[Unit] = {
    for {
      _ <- sitemapService.invalidate()
      _ <- contentTypeService.invalidate()
      _ <- feedService.invalidate()
      _ <- IO(logger.info(s"All caches are invalidated."))
    } yield ()
  }

}
