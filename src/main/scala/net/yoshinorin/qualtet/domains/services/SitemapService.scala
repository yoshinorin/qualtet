package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.sitemaps.{SitemapsRepository, Url}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase
import net.yoshinorin.qualtet.utils.Cache

class SitemapService(
  sitemapRepository: SitemapsRepository,
  cache: Cache[String, Seq[Url]]
)(
  implicit doobieContext: DoobieContextBase
) {

  private val cacheKey = "sitemaps-full-cache"

  def get(): IO[Seq[Url]] = {

    // TODO: updated_at convert to YYYY-MM-DD
    def fromDB(): IO[Seq[Url]] = {
      for {
        x <- sitemapRepository.get.transact(doobieContext.transactor)
      } yield (x, cache.put(cacheKey, x))._1
    }

    val maybeSitemap = cache.get(cacheKey)
    maybeSitemap match {
      case Some(x: Seq[Url]) => IO(x)
      case _ => fromDB()
    }

  }

}
