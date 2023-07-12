package net.yoshinorin.qualtet.domains.sitemaps

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

// https://www.sitemaps.org/index.html
opaque type Loc = String
object Loc {
  given codecLastMod: JsonValueCodec[Loc] = JsonCodecMaker.make

  def apply(value: String): Loc = {
    // TODO: add url to prefix like https://example.com/....
    // TODO: validate URL or not
    value
  }

  extension (loc: Loc) {
    def value: String = loc
  }
}

opaque type LastMod = String
object LastMod {
  given codecLastMod: JsonValueCodec[LastMod] = JsonCodecMaker.make

  def apply(value: String): LastMod = {
    // TODO: validate YYYY-MM-DD string or not
    Instant
      .ofEpochSecond(value.toLong)
      .atZone(ZoneId.of("GMT"))
      .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
  }

  extension (lastMod: LastMod) {
    def value: String = lastMod
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
  given codecUrl: JsonValueCodec[Url] = JsonCodecMaker.make
  given codecUrls: JsonValueCodec[Seq[Url]] = JsonCodecMaker.make
}
