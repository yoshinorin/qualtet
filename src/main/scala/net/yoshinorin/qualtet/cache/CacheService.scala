package net.yoshinorin.qualtet.cache

import cats.effect.IO
import net.yoshinorin.qualtet.domains.sitemaps.SitemapService
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import org.slf4j.LoggerFactory

class CacheService(
  sitemapService: SitemapService,
  contentTypeService: ContentTypeService
) {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def invalidateAll(): IO[Unit] = {
    sitemapService.invalidate()
    contentTypeService.invalidate()
    IO(logger.info(s"All caches are invalidated."))
  }

}
