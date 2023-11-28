package net.yoshinorin.qualtet.domains.sitemaps

trait SitemapsRepository[F[_]] {
  def get(): F[Seq[Url]]
}
