package net.yoshinorin.qualtet.domains.sitemaps

import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.domains.Cacheable

import scala.annotation.nowarn

class SitemapService[F[_]: Monad, G[_]: Monad @nowarn](
  sitemapRepositoryAdapter: SitemapRepositoryAdapter[G],
  cache: CacheModule[F, String, Seq[Url]]
)(using executer: Executer[F, G])
    extends Cacheable[F] {

  private val CACHE_KEY = "SITEMAPS_FULL_CACHE"

  def get(): F[Seq[Url]] = {

    def fromDB(): F[Seq[Url]] = {
      executer.transact(sitemapRepositoryAdapter.get)
    }

    for {
      maybeSitemaps <- cache.get(CACHE_KEY)
      sitemaps <- maybeSitemaps match {
        case Some(urls: Seq[Url]) => Monad[F].pure(urls)
        case _ =>
          for {
            dbSitemaps <- fromDB()
            _ <- cache.put(CACHE_KEY, dbSitemaps)
          } yield dbSitemaps
      }
    } yield sitemaps
  }

  def invalidate(): F[Unit] = {
    cache.invalidate()
  }

}
