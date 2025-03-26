package net.yoshinorin.qualtet.domains.sitemaps

import cats.data.ContT
import cats.Monad
import cats.implicits.*

class SitemapRepositoryAdapter[F[_]: Monad](
  sitemapRepository: SitemapsRepository[F]
) {

  def get: ContT[F, Seq[Url], Seq[Url]] = {
    ContT.apply[F, Seq[Url], Seq[Url]] { next =>
      sitemapRepository.get().map { x =>
        x.map { s =>
          Url(s.loc, s.lastMod)
        }
      }
    }
  }

}
