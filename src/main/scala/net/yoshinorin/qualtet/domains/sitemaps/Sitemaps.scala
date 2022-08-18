package net.yoshinorin.qualtet.domains.sitemaps

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

// https://www.sitemaps.org/index.html
class Sitemap {}

final case class Loc(value: String) extends AnyVal
object Loc {
  implicit val encodeLoc: Encoder[Loc] = Encoder[String].contramap(_.value)
  implicit val decodeLoc: Decoder[Loc] = Decoder[String].map(Loc.apply)

  def apply(value: String): Loc = {
    // TODO: add url to prefix like https://example.com/....
    // TODO: validate URL or not
    new Loc(value)
  }
}

final case class LastMod(value: String) extends AnyVal
object LastMod {
  implicit val encodeLastMod: Encoder[LastMod] = Encoder[String].contramap(_.value)
  implicit val decodeLastMod: Decoder[LastMod] = Decoder[String].map(LastMod.apply)

  def apply(value: String): LastMod = {
    // TODO: validate YYYY-MM-DD string or not
    val sitemapTagFormatDate = Instant
      .ofEpochSecond(value.toLong)
      .atZone(ZoneId.of("GMT"))
      .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    new LastMod(sitemapTagFormatDate)
  }
}

// TODO: consider rename to Sitemaps
final case class Url(
  loc: Loc,
  lastMod: LastMod
  // TODO: consider below tags
  // https://www.sitemaps.org/protocol.html
  // changefreq: Option[String] = Option("daily"),
  // priolity: Option[Double] = Option(1.0)
)

object Url {
  implicit val encodeUrl: Encoder[Url] = deriveEncoder[Url]
  implicit val encodeUrls: Encoder[List[Url]] = Encoder.encodeList[Url]
  implicit val decodeUrl: Decoder[Url] = deriveDecoder[Url]
  implicit val decodeUrls: Decoder[List[Url]] = Decoder.decodeList[Url]
}
