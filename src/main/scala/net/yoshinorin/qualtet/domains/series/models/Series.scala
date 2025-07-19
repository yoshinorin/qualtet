package net.yoshinorin.qualtet.domains.series

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{UlidConvertible, ValueExtender}
import net.yoshinorin.qualtet.domains.errors.InvalidPath

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

  private lazy val unusableChars = "[:?#@!$&'()*+,;=<>\"\\\\^`{}|~]"
  private lazy val invalidPercentRegex = ".*%(?![0-9A-Fa-f]{2}).*"

  def apply(value: String): SeriesPath = {
    if (unusableChars.r.findFirstMatchIn(value).isDefined) {
      throw InvalidPath(detail = s"Invalid character contains: ${value}")
    }

    if (invalidPercentRegex.r.matches(value)) {
      throw InvalidPath(detail = s"Invalid percent encoding in path: ${value}")
    }

    if (!value.startsWith("/")) {
      s"/${value}"
    } else {
      value
    }
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
