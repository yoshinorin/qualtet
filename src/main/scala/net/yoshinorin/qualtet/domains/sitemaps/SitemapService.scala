package net.yoshinorin.qualtet.domains.sitemaps

import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.domains.Cacheable

class SitemapService[F[_]: Monad](
  sitemapRepositoryAdapter: SitemapRepositoryAdapter[F],
  cache: CacheModule[String, Seq[Url]]
)(using executer: Executer[F, IO])
    extends Cacheable {

  private val cacheKey = "sitemaps-full-cache"

  def get(): IO[Seq[Url]] = {

    def fromDB(): IO[Seq[Url]] = {
      executer.transact(sitemapRepositoryAdapter.get)
    }

    cache.get(cacheKey) match {
      case Some(x: Seq[Url]) => IO(x)
      case _ =>
        for {
          urls <- fromDB()
        } yield {
          cache.put(cacheKey, urls)
          urls
        }
    }
  }

  def invalidate(): IO[Unit] = {
    IO(cache.invalidate())
  }

}
