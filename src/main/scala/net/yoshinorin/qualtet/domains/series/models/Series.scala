package net.yoshinorin.qualtet.domains.series

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{FromTrustedSource, Request, UlidConvertible, ValueExtender}
import net.yoshinorin.qualtet.domains.errors.{DomainError, InvalidPath}

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

  def apply(value: String): Either[InvalidPath, SeriesPath] = {
    if (unusableChars.r.findFirstMatchIn(value).isDefined) {
      return Left(InvalidPath(detail = s"Invalid character contains: ${value}"))
    }
    if (invalidPercentRegex.r.matches(value)) {
      return Left(InvalidPath(detail = s"Invalid percent encoding in path: ${value}"))
    }
    val normalized = if (!value.startsWith("/")) s"/${value}" else value
    Right(normalized)
  }

  private def unsafeFrom(value: String): SeriesPath = {
    if (!value.startsWith("/")) s"/${value}" else value
  }

  given fromTrustedSource: FromTrustedSource[SeriesPath] with {
    def fromTrusted(value: String): SeriesPath = unsafeFrom(value)
  }
}

final case class Series(
  id: SeriesId = SeriesId.apply(),
  name: SeriesName,
  path: SeriesPath,
  title: String,
  description: Option[String]
) extends Request[Series] {
  def postDecode: Either[DomainError, Series] = {
    SeriesPath(path.value).map(seriesPath => Series(id, name, seriesPath, title, description))
  }
}

object Series {
  given codecSeries: JsonValueCodec[Series] = JsonCodecMaker.make
  given codecSeqSeries: JsonValueCodec[Seq[Series]] = JsonCodecMaker.make
}
