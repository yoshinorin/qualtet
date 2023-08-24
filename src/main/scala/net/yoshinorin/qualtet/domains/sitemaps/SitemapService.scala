package net.yoshinorin.qualtet.domains.sitemaps

import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.actions.Action.*
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.infrastructure.db.Transactor
import net.yoshinorin.qualtet.domains.Cacheable

class SitemapService[M[_]: Monad](
  sitemapRepository: SitemapsRepository[M],
  cache: CacheModule[String, Seq[Url]]
)(using transactor: Transactor[M])
    extends Cacheable {

  private val cacheKey = "sitemaps-full-cache"

  def getActions: Action[Seq[Url]] = {
    Continue(sitemapRepository.get(), Action.done[Seq[Url]])
  }

  def get(): IO[Seq[Url]] = {
    cache.get(cacheKey) match {
      case Some(x: Seq[Url]) => IO(x)
      case _ =>
        for {
          x <- transactor.transact(getActions)
        } yield (x, cache.put(cacheKey, x))._1
    }
  }

  def invalidate(): IO[Unit] = {
    IO(cache.invalidate())
  }

}
