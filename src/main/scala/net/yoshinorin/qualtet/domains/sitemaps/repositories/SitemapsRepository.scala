package net.yoshinorin.qualtet.domains.sitemaps

trait SitemapsRepository[F[_]] {
  def get(): F[Seq[UrlReadModel]]
}

object SitemapsRepository {

  import org.typelevel.doobie.{ConnectionIO, Get, Read}

  given SitemapsRepository: SitemapsRepository[ConnectionIO] = {
    new SitemapsRepository[ConnectionIO] {

      given lastModGet: Get[LastMod] = Get[String].map(LastMod.fromTrusted)

      given sitemapsRead: Read[UrlReadModel] =
        Read[(String, LastMod)].map { case (loc, mod) =>
          UrlReadModel(
            Loc(loc),
            mod
          )
        }

      override def get(): ConnectionIO[Seq[UrlReadModel]] = {
        SitemapsQuery.get.to[Seq]
      }

    }
  }

}
