package net.yoshinorin.qualtet.domains.series

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{Request, UlidConvertible, ValueExtender}
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

  /**
   * Create a SeriesPath from a trusted source (e.g., database) without validation.
   *
   * This method should ONLY be used in Repository layer when reading data from the database.
   * Database data is assumed to be already validated at write time, so we skip validation
   * for performance reasons while still applying normalization for consistency.
   *
   * DO NOT use this method in:
   * - HTTP request handlers
   * - User input processing
   * - Any external data source
   *
   * @param value The raw string value from a trusted source
   * @return The normalized SeriesPath without validation
   */
  private[series] def unsafe(value: String): SeriesPath = {
    if (!value.startsWith("/")) s"/${value}" else value
  }
}

final case class Series(
  id: SeriesId = SeriesId.apply(),
  name: SeriesName,
  path: SeriesPath,
  title: String,
  description: Option[String]
) extends Request[Series] {
  def postDecode: Series = {
    // TODO: Refactor to return Either instead of throwing
    val validatedPath = SeriesPath(path.value) match {
      case Right(p) => p
      case Left(error) => throw error
    }

    Series(id, name, validatedPath, title, description)
  }
}

object Series {
  given codecSeries: JsonValueCodec[Series] = JsonCodecMaker.make
  given codecSeqSeries: JsonValueCodec[Seq[Series]] = JsonCodecMaker.make
}
