package net.yoshinorin.qualtet.cache

import cats.Monad
import cats.effect.IO
import net.yoshinorin.qualtet.domains.sitemaps.SitemapService
import net.yoshinorin.qualtet.domains.tags.TagService
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.domains.feeds.FeedService
import org.slf4j.LoggerFactory

class CacheService[F[_]: Monad](
  sitemapService: SitemapService[F],
  tagsService: TagService[F],
  contentTypeService: ContentTypeService[F],
  feedService: FeedService[F]
) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def invalidateAll(): IO[Unit] = {
    for {
      _ <- sitemapService.invalidate()
      _ <- tagsService.invalidate()
      _ <- contentTypeService.invalidate()
      _ <- feedService.invalidate()
      _ <- IO(logger.info(s"All caches are invalidated."))
    } yield ()
  }

}
