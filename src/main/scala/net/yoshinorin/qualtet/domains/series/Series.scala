package net.yoshinorin.qualtet.domains.series

import wvlet.airframe.ulid.ULID
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.Request
import net.yoshinorin.qualtet.message.Fail.BadRequest
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.domains.articles.ResponseArticle

final case class SeriesId(value: String = ULID.newULIDString.toLower) extends AnyVal
object SeriesId {
  given codecSeriesId: JsonValueCodec[SeriesId] = JsonCodecMaker.make

  def apply(value: String): SeriesId = {
    val _ = ULID.fromString(value)
    new SeriesId(value)
  }
}

final case class SeriesName(value: String) extends AnyVal
object SeriesName {
  given codecSeriesName: JsonValueCodec[SeriesName] = JsonCodecMaker.make
}

final case class Series(
  id: SeriesId = new SeriesId,
  name: SeriesName,
  title: String,
  description: Option[String]
)

object Series {
  given codecSeries: JsonValueCodec[Series] = JsonCodecMaker.make
  given codecSeqSeries: JsonValueCodec[Seq[Series]] = JsonCodecMaker.make
}

final case class RequestSeries(
  name: SeriesName,
  title: String,
  description: Option[String]
) extends Request[RequestSeries] {
  def postDecode: RequestSeries = {
    // TODO: improve
    name.value.trimOrThrow(BadRequest("name is required"))
    new RequestSeries(
      name = name,
      title = title.trimOrThrow(BadRequest("title is required")),
      description = description
    )
  }
}

object RequestSeries {
  given codecRequestSeries: JsonValueCodec[RequestSeries] = JsonCodecMaker.make
  given codecRequestListSeries: JsonValueCodec[List[RequestSeries]] = JsonCodecMaker.make

  def apply(name: SeriesName, title: String, description: Option[String]): RequestSeries = {
    // TODO: improve
    name.value.trimOrThrow(BadRequest("name is required"))
    new RequestSeries(
      name = name,
      title = title.trimOrThrow(BadRequest("title is required")),
      description = description
    )
  }
}

final case class ResponseSeries(
  id: SeriesId,
  name: SeriesName,
  title: String,
  description: Option[String],
  articles: Seq[ResponseArticle]
)

object ResponseSeries {
  given codecResponseSeries: JsonValueCodec[ResponseSeries] = JsonCodecMaker.make
  given codecResponseSeriesWithCount: JsonValueCodec[Seq[ResponseSeries]] = JsonCodecMaker.make
}
