package net.yoshinorin.qualtet.domains.sitemaps

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase
import RepositoryRequests.Get

class SitemapService(cache: CacheModule[String, Seq[Url]])(implicit doobieContext: DoobieContextBase) {

  private val cacheKey = "sitemaps-full-cache"

  def get(): IO[Seq[Url]] = {

    def makeRequest(): (Get, Seq[Url] => Seq[Url]) = {
      val request = Get()
      (request, Seq[Url])
    }

    def fromDb(): IO[Seq[Url]] = {
      val (request, _) = makeRequest()
      SitemapsRepository.dispatch(request).transact(doobieContext.transactor)
    }

    cache.get(cacheKey) match {
      case Some(x: Seq[Url]) => IO(x)
      case _ =>
        for {
          x <- fromDb()
        } yield (x, cache.put(cacheKey, x))._1
    }
  }

}
