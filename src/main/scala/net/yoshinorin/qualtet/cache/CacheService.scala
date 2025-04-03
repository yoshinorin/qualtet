package net.yoshinorin.qualtet.cache

import cats.Monad
import cats.effect.IO
import net.yoshinorin.qualtet.domains.sitemaps.SitemapService
import net.yoshinorin.qualtet.domains.tags.TagService
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.domains.feeds.FeedService
import org.typelevel.log4cats.{LoggerFactory => Log4CatsLoggerFactory, SelfAwareStructuredLogger}

class CacheService[F[_]: Monad](
  sitemapService: SitemapService[F],
  tagsService: TagService[F],
  contentTypeService: ContentTypeService[F],
  feedService: FeedService[F]
)(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  private val logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  def invalidateAll(): IO[Unit] = {
    for {
      _ <- sitemapService.invalidate()
      _ <- tagsService.invalidate()
      _ <- contentTypeService.invalidate()
      _ <- feedService.invalidate()
      _ <- logger.info(s"All caches are invalidated.")
    } yield ()
  }

}
