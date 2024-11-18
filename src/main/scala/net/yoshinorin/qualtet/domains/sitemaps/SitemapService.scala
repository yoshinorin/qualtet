package net.yoshinorin.qualtet.domains.sitemaps

import cats.data.ContT
import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.domains.Cacheable

class SitemapService[F[_]: Monad](
  sitemapRepository: SitemapsRepository[F],
  cache: CacheModule[String, Seq[Url]]
)(using executer: Executer[F, IO])
    extends Cacheable {

  private val cacheKey = "sitemaps-full-cache"

  def getActions: ContT[F, Seq[Url], Seq[Url]] = {
    ContT.apply[F, Seq[Url], Seq[Url]] { next =>
      sitemapRepository.get().map { x =>
        x.map { s =>
          Url(s.loc, s.lastMod)
        }
      }
    }
  }

  def get(): IO[Seq[Url]] = {
    cache.get(cacheKey) match {
      case Some(x: Seq[Url]) => IO(x)
      case _ =>
        for {
          x <- executer.transact(getActions)
        } yield (x, cache.put(cacheKey, x))._1
    }
  }

  def invalidate(): IO[Unit] = {
    IO(cache.invalidate())
  }

}
