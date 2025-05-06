package net.yoshinorin.qualtet.domains.series

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{UlidConvertible, ValueExtender}
import net.yoshinorin.qualtet.domains.errors.{InvalidPath, UnexpectedException}
import java.net.{URI, URISyntaxException}

opaque type SeriesId = String
object SeriesId extends ValueExtender[SeriesId] with UlidConvertible[SeriesId] {
  given codecSeriesId: JsonValueCodec[SeriesId] = JsonCodecMaker.make
}

opaque type SeriesName = String
object SeriesName extends ValueExtender[SeriesName] {
  given codecSeriesName: JsonValueCodec[SeriesName] = JsonCodecMaker.make
  def apply(value: String): SeriesName = value
}

opaque type SeriesPath = String
object SeriesPath extends ValueExtender[SeriesPath] {
  given codecSeriesPath: JsonValueCodec[SeriesPath] = JsonCodecMaker.make

  def apply(value: String): SeriesPath = {
    // NOTE: Probably follows https://www.ietf.org/rfc/rfc3986.txt
    try {
      URI(value);
    } catch {
      case _: URISyntaxException => throw InvalidPath(detail = s"Invalid character contains: ${value}")
      case _ => throw UnexpectedException()
    }
    value
  }
}

final case class Series(
  id: SeriesId = SeriesId.apply(),
  name: SeriesName,
  path: SeriesPath,
  title: String,
  description: Option[String]
)

object Series {
  given codecSeries: JsonValueCodec[Series] = JsonCodecMaker.make
  given codecSeqSeries: JsonValueCodec[Seq[Series]] = JsonCodecMaker.make
}
