package net.yoshinorin.qualtet.domains.sitemaps

trait SitemapsRepository[F[_]] {
  def get(): F[Seq[UrlReadModel]]
}

object SitemapsRepository {

  import doobie.Read
  import doobie.ConnectionIO

  given SitemapsRepository: SitemapsRepository[ConnectionIO] = {
    new SitemapsRepository[ConnectionIO] {

      given tagRead: Read[UrlReadModel] =
        Read[(String, String)].map { case (loc, mod) => UrlReadModel(Loc(loc), LastMod(mod)) }

      override def get(): ConnectionIO[Seq[UrlReadModel]] = {
        SitemapsQuery.get.to[Seq]
      }

    }
  }

}
