package net.yoshinorin.qualtet.domains.series

import wvlet.airframe.ulid.ULID
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.domains.Request
import net.yoshinorin.qualtet.domains.contents.Path
import net.yoshinorin.qualtet.message.Fail.BadRequest
import net.yoshinorin.qualtet.syntax._

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
  given codecSeries: JsonValueCodec[Series] = JsonCodecMaker.make
  given codecSeqSeries: JsonValueCodec[Seq[Series]] = JsonCodecMaker.make
}

final case class RequestSeries(
  path: Path,
  title: String,
  description: Option[String]
) extends Request[RequestSeries] {
  def postDecode: RequestSeries = {
    new RequestSeries(
      path = path,
      title = title.trimOrThrow(BadRequest("title is required")),
      description = description
    )
  }
}

object RequestSeries {
  given codecRequestSeries: JsonValueCodec[RequestSeries] = JsonCodecMaker.make
  given codecRequestListSeries: JsonValueCodec[List[RequestSeries]] = JsonCodecMaker.make

  def apply(path: Path, title: String, description: Option[String]): RequestSeries = {
    new RequestSeries(
      path = path,
      title = title.trimOrThrow(BadRequest("title is required")),
      description = description
    )
  }
}