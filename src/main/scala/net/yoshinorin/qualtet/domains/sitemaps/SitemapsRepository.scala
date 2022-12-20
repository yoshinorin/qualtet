package net.yoshinorin.qualtet.domains.sitemaps

trait SitemapsRepository[M[_]] {
  def get(): M[Seq[Url]]
}
