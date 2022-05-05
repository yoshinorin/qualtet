package net.yoshinorin.qualtet.domains.sitemaps

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase
import RepositoryRequests.Get

class SitemapService(
  sitemapRepository: SitemapsRepository,
  cache: CacheModule[String, Seq[Url]]
)(
  implicit doobieContext: DoobieContextBase
) {

  private val cacheKey = "sitemaps-full-cache"

  private def makeRequest(): (Get, Seq[Url] => Seq[Url]) = {
    val requests = Get()
    (requests, Seq[Url])
  }

  private def fromDb(): IO[Seq[Url]] = {
    val (request, condition) = this.makeRequest()
    sitemapRepository.dispatch(request).transact(doobieContext.transactor)
  }

  def get(): IO[Seq[Url]] = {
    cache.get(cacheKey) match {
      case Some(x: Seq[Url]) => IO(x)
      case _ =>
        for {
          x <- fromDb()
        } yield (x, cache.put(cacheKey, x))._1
    }
  }

}