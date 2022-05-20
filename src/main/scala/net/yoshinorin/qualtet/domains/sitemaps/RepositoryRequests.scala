package net.yoshinorin.qualtet.domains.sitemaps

import net.yoshinorin.qualtet.domains.repository.requests._

trait SitemapRepositoryRequest[T] extends RepositoryRequest[T]
final case class Get() extends SitemapRepositoryRequest[Seq[Url]]
