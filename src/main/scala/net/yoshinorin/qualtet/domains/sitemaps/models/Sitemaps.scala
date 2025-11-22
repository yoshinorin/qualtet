package net.yoshinorin.qualtet.domains.sitemaps

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.ValueExtender
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

  /**
   * Create a LastMod from a trusted source (e.g., database) without validation.
   *
   * This method should ONLY be used in Repository layer when reading data from the database.
   * Database data is assumed to be already validated at write time, so we skip validation
   * for performance reasons.
   *
   * DO NOT use this method in:
   * - HTTP request handlers
   * - User input processing
   * - Any external data source
   *
   * @param value The raw string value from a trusted source (should be formatted as YYYY-MM-DD)
   * @return The LastMod without validation
   */
  private[sitemaps] def unsafe(value: String): LastMod = value
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
