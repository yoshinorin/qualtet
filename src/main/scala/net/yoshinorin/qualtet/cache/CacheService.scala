package net.yoshinorin.qualtet.cache

import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.sitemaps.SitemapService
import net.yoshinorin.qualtet.domains.tags.TagService
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.domains.feeds.FeedService
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class CacheService[F[_]: Monad, G[_]: Monad @nowarn](
  sitemapService: SitemapService[F, G],
  tagsService: TagService[F, G],
  contentTypeService: ContentTypeService[F, G],
  feedService: FeedService[F, G]
)(using loggerFactory: Log4CatsLoggerFactory[F]) {

  private val logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  def invalidateAll(): F[Unit] = {
    for {
      _ <- sitemapService.invalidate()
      _ <- tagsService.invalidate()
      _ <- contentTypeService.invalidate()
      _ <- feedService.invalidate()
      _ <- logger.info(s"All caches are invalidated.")
    } yield ()
  }

}
