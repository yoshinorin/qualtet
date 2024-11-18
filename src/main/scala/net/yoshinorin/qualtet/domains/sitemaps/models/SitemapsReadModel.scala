package net.yoshinorin.qualtet.domains.sitemaps

// TODO: consider rename to Sitemaps
final case class UrlReadModel(
  loc: Loc,
  lastMod: LastMod
  // TODO: consider add below tags
  // https://www.sitemaps.org/protocol.html
  // changefreq: Option[String] = Option("daily"),
  // priolity: Option[Double] = Option(1.0)
)
