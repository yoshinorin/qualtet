package net.yoshinorin.qualtet.domains.series

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.Request
import net.yoshinorin.qualtet.domains.errors.{SeriesNameRequired, SeriesTitleRequired}
import net.yoshinorin.qualtet.syntax.*

final case class RequestSeries(
  name: SeriesName,
  title: String,
  description: Option[String]
) extends Request[RequestSeries] {
  def postDecode: RequestSeries = {
    // TODO: improve
    name.value.trimOrThrow(SeriesNameRequired(detail = "name is required"))
    new RequestSeries(
      name = name,
      title = title.trimOrThrow(SeriesTitleRequired(detail = "title is required")),
      description = description
    )
  }
}

object RequestSeries {
  given codecRequestSeries: JsonValueCodec[RequestSeries] = JsonCodecMaker.make
  given codecRequestListSeries: JsonValueCodec[List[RequestSeries]] = JsonCodecMaker.make

  def apply(name: SeriesName, title: String, description: Option[String]): RequestSeries = {
    // TODO: improve
    name.value.trimOrThrow(SeriesNameRequired(detail = "name is required"))
    new RequestSeries(
      name = name,
      title = title.trimOrThrow(SeriesTitleRequired(detail = "title is required")),
      description = description
    )
  }
}
