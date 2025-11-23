package net.yoshinorin.qualtet.domains.sitemaps

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{FromTrustedSource, ValueExtender}
import net.yoshinorin.qualtet.domains.errors.InvalidLastMod

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import scala.util.{Failure, Success, Try}

// https://www.sitemaps.org/index.html
opaque type Loc = String
object Loc extends ValueExtender[Loc] {
  given codecLastMod: JsonValueCodec[Loc] = JsonCodecMaker.make

  def apply(value: String): Loc = {
    // TODO: add url to prefix like https://example.com/....
    // TODO: validate URL or not
    value
  }
}

opaque type LastMod = String
object LastMod extends ValueExtender[LastMod] {
  given codecLastMod: JsonValueCodec[LastMod] = new JsonValueCodec[LastMod] {
    def decodeValue(in: JsonReader, default: LastMod): LastMod = {
      // Custom codec required: jsoniter-scala cannot convert between String and opaque types by default.
      // JSON already contains formatted date string (YYYY-MM-DD), use it directly
      in.readString(null)
    }

    def encodeValue(x: LastMod, out: JsonWriter): Unit = {
      out.writeVal(x.value)
    }

    def nullValue: LastMod = null.asInstanceOf[LastMod]
  }

  def apply(value: String): Either[InvalidLastMod, LastMod] = {
    // TODO: validate YYYY-MM-DD string or not
    Try(value.toLong) match {
      case Success(epochSecond) =>
        Try {
          Instant
            .ofEpochSecond(epochSecond)
            .atZone(ZoneId.of("GMT"))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } match {
          case Success(formattedDate) => Right(formattedDate)
          case Failure(e) => Left(InvalidLastMod(detail = s"Invalid epoch second: $value. ${e.getMessage}"))
        }
      case Failure(e) => Left(InvalidLastMod(detail = s"Invalid last modified value: $value. Must be a valid epoch second."))
    }
  }

  private def unsafeFrom(value: String): LastMod = value

  given fromTrustedSource: FromTrustedSource[LastMod] with {
    def fromTrusted(value: String): LastMod = unsafeFrom(value)
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
