package net.yoshinorin.qualtet.domains.series

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{UlidConvertible, ValueExtender}

opaque type SeriesId = String
object SeriesId extends ValueExtender[SeriesId] with UlidConvertible[SeriesId] {
  given codecSeriesId: JsonValueCodec[SeriesId] = JsonCodecMaker.make
}

opaque type SeriesName = String
object SeriesName extends ValueExtender[SeriesName] {
  given codecSeriesName: JsonValueCodec[SeriesName] = JsonCodecMaker.make

  def apply(value: String): SeriesName = value
}

final case class Series(
  id: SeriesId = SeriesId.apply(),
  name: SeriesName,
  title: String,
  description: Option[String]
)

object Series {
  given codecSeries: JsonValueCodec[Series] = JsonCodecMaker.make
  given codecSeqSeries: JsonValueCodec[Seq[Series]] = JsonCodecMaker.make
}
