package net.yoshinorin.qualtet.domains.sitemaps

import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.domains.Cacheable

class SitemapService[F[_]: Monad](
  sitemapRepositoryAdapter: SitemapRepositoryAdapter[F],
  cache: CacheModule[IO, String, Seq[Url]]
)(using executer: Executer[F, IO])
    extends Cacheable {

  private val cacheKey = "sitemaps-full-cache"

  def get(): IO[Seq[Url]] = {

    def fromDB(): IO[Seq[Url]] = {
      executer.transact(sitemapRepositoryAdapter.get)
    }

    for {
      maybeSitemaps <- cache.get(cacheKey)
      sitemaps <- maybeSitemaps match {
        case Some(urls: Seq[Url]) => IO.pure(urls)
        case _ =>
          for {
            dbSitemaps <- fromDB()
            _ <- cache.put(cacheKey, dbSitemaps)
          } yield dbSitemaps
      }
    } yield sitemaps
  }

  def invalidate(): IO[Unit] = {
    cache.invalidate()
  }

}
