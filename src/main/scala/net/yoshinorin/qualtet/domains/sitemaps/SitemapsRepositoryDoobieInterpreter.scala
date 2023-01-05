package net.yoshinorin.qualtet.domains.sitemaps

import doobie.Read
import doobie.ConnectionIO

class SitemapsRepositoryDoobieInterpreter extends SitemapsRepository[ConnectionIO] {

  implicit val tagRead: Read[Url] =
    Read[(String, String)].map { case (loc, mod) => Url(Loc(loc), LastMod(mod)) }

  override def get(): ConnectionIO[Seq[Url]] = {
    SitemapsQuery.get.to[Seq]
  }
}
