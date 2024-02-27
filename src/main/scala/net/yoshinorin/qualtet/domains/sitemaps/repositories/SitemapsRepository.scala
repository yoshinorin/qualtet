package net.yoshinorin.qualtet.domains.sitemaps

trait SitemapsRepository[F[_]] {
  def get(): F[Seq[Url]]
}

object SitemapsRepository {

  import doobie.Read
  import doobie.ConnectionIO

  given SitemapsRepository: SitemapsRepository[ConnectionIO] = {
    new SitemapsRepository[ConnectionIO] {

      given tagRead: Read[Url] =
        Read[(String, String)].map { case (loc, mod) => Url(Loc(loc), LastMod(mod)) }

      override def get(): ConnectionIO[Seq[Url]] = {
        SitemapsQuery.get.to[Seq]
      }

    }
  }

}
