package net.yoshinorin.qualtet.domains.series

import wvlet.airframe.ulid.ULID
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.syntax._
import net.yoshinorin.qualtet.domains.contents.Path

final case class SeriesId(value: String = ULID.newULIDString.toLower) extends AnyVal
object SeriesId {
  given codecSeriesId: JsonValueCodec[SeriesId] = JsonCodecMaker.make

  def apply(value: String): SeriesId = {
    val _ = ULID.fromString(value)
    new SeriesId(value)
  }
}

final case class Series(
  id: SeriesId = new SeriesId,
  path: Path,
  title: String,
  description: Option[String]
)

object Series {
  given seriesContent: JsonValueCodec[Series] = JsonCodecMaker.make
  given seriesContents: JsonValueCodec[List[Series]] = JsonCodecMaker.make
}
