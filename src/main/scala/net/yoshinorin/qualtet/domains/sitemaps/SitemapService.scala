package net.yoshinorin.qualtet.domains.sitemaps

import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor.Aux
import net.yoshinorin.qualtet.domains.DoobieAction._
import net.yoshinorin.qualtet.domains.{DoobieAction, DoobieContinue}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.infrastructure.db.DataBaseContext
import net.yoshinorin.qualtet.domains.Cacheable

class SitemapService(
  sitemapRepository: SitemapsRepository[ConnectionIO],
  cache: CacheModule[String, Seq[Url]]
)(dbContext: DataBaseContext[Aux[IO, Unit]])
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
          x <- getActions.perform.andTransact(dbContext)
        } yield (x, cache.put(cacheKey, x))._1
    }
  }

  def invalidate(): IO[Unit] = {
    IO(cache.invalidate())
  }

}
