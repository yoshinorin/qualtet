package net.yoshinorin.qualtet.domains.sitemaps

import cats.effect.IO
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.DoobieAction._
import net.yoshinorin.qualtet.domains.{DoobieAction, DoobieContinue}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import net.yoshinorin.qualtet.domains.Cacheable

class SitemapService(
  sitemapRepository: SitemapsRepository[ConnectionIO],
  cache: CacheModule[String, Seq[Url]]
)(doobieContext: DoobieContext)
    extends Cacheable {

  private val cacheKey = "sitemaps-full-cache"

  def getActions: DoobieAction[Seq[Url]] = {
    DoobieContinue(sitemapRepository.get(), DoobieAction.buildDoneWithoutAnyHandle[Seq[Url]])
  }

  def get(): IO[Seq[Url]] = {
    cache.get(cacheKey) match {
      case Some(x: Seq[Url]) => IO(x)
      case _ =>
        for {
          x <- getActions.perform.andTransact(doobieContext)
        } yield (x, cache.put(cacheKey, x))._1
    }
  }

  def invalidate(): IO[Unit] = {
    IO(cache.invalidate())
  }

}
