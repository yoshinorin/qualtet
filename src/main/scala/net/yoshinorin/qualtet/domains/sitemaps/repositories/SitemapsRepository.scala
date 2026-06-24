package net.yoshinorin.qualtet.domains.sitemaps

trait SitemapsRepository[F[_]] {
  def get(): F[Seq[UrlReadModel]]
}

object SitemapsRepository {

  import org.typelevel.doobie.Read
  import org.typelevel.doobie.ConnectionIO

  given SitemapsRepository: SitemapsRepository[ConnectionIO] = {
    new SitemapsRepository[ConnectionIO] {

      given sitemapsRead: Read[UrlReadModel] =
        Read[(String, String)].map { case (loc, mod) =>
          UrlReadModel(
            Loc(loc),
            LastMod.fromTrusted(mod)
          )
        }

      override def get(): ConnectionIO[Seq[UrlReadModel]] = {
        SitemapsQuery.get.to[Seq]
      }

    }
  }

}
