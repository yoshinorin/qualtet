package net.yoshinorin.qualtet.domains.sitemaps

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

// https://www.sitemaps.org/index.html
final case class Loc(value: String) extends AnyVal
object Loc {
  implicit val codecLastMod: JsonValueCodec[Loc] = JsonCodecMaker.make

  def apply(value: String): Loc = {
    // TODO: add url to prefix like https://example.com/....
    // TODO: validate URL or not
    new Loc(value)
  }
}

final case class LastMod(value: String) extends AnyVal
object LastMod {
  implicit val codecLastMod: JsonValueCodec[LastMod] = JsonCodecMaker.make

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
  // TODO: consider add below tags
  // https://www.sitemaps.org/protocol.html
  // changefreq: Option[String] = Option("daily"),
  // priolity: Option[Double] = Option(1.0)
)

object Url {
  implicit val codecUrl: JsonValueCodec[Url] = JsonCodecMaker.make
  implicit val codecUrls: JsonValueCodec[Seq[Url]] = JsonCodecMaker.make
}
