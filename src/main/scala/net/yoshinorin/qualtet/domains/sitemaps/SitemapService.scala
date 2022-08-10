package net.yoshinorin.qualtet.domains.sitemaps

import cats.effect.IO
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import net.yoshinorin.qualtet.domains.Cacheable

class SitemapService(cache: CacheModule[String, Seq[Url]])(doobieContext: DoobieContext) extends Cacheable {

  private val cacheKey = "sitemaps-full-cache"

  def get(): IO[Seq[Url]] = {

    def actions: Action[Seq[Url]] = {
      Continue(Get(), Action.buildNext[Seq[Url]])
    }

    cache.get(cacheKey) match {
      case Some(x: Seq[Url]) => IO(x)
      case _ =>
        for {
          x <- actions.perform.andTransact(doobieContext)
        } yield (x, cache.put(cacheKey, x))._1
    }
  }

  def invalidate(): IO[Unit] = {
    IO(cache.invalidate())
  }

}
