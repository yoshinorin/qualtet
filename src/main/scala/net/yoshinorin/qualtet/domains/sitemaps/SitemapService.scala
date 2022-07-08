package net.yoshinorin.qualtet.domains.sitemaps

import cats.effect.IO
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue, Done}
import net.yoshinorin.qualtet.cache.CacheModule
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase

class SitemapService(cache: CacheModule[String, Seq[Url]])(doobieContext: DoobieContextBase) {

  private val cacheKey = "sitemaps-full-cache"

  def get(): IO[Seq[Url]] = {

    def actions(): Action[Seq[Url]] = {
      val request = Get()
      val resultHandler: Seq[Url] => Action[Seq[Url]] = (resultHandler: Seq[Url]) => { Done(resultHandler) }
      Continue(request, resultHandler)
    }

    cache.get(cacheKey) match {
      case Some(x: Seq[Url]) => IO(x)
      case _ =>
        for {
          x <- actions().perform().andTransact()(doobieContext)
        } yield (x, cache.put(cacheKey, x))._1
    }
  }

}
