package net.yoshinorin.qualtet.domains.series

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.Request
import net.yoshinorin.qualtet.domains.errors.{SeriesNameRequired, SeriesTitleRequired}
import net.yoshinorin.qualtet.syntax.*

final case class SeriesRequestModel(
  name: SeriesName,
  title: String,
  description: Option[String]
) extends Request[SeriesRequestModel] {
  def postDecode: SeriesRequestModel = {
    // TODO: improve
    name.value.trimOrThrow(SeriesNameRequired(detail = "name is required"))
    new SeriesRequestModel(
      name = name,
      title = title.trimOrThrow(SeriesTitleRequired(detail = "title is required")),
      description = description
    )
  }
}

object SeriesRequestModel {
  given codecRequestSeries: JsonValueCodec[SeriesRequestModel] = JsonCodecMaker.make
  given codecRequestListSeries: JsonValueCodec[List[SeriesRequestModel]] = JsonCodecMaker.make

  def apply(name: SeriesName, title: String, description: Option[String]): SeriesRequestModel = {
    // TODO: improve
    name.value.trimOrThrow(SeriesNameRequired(detail = "name is required"))
    new SeriesRequestModel(
      name = name,
      title = title.trimOrThrow(SeriesTitleRequired(detail = "title is required")),
      description = description
    )
  }
}
