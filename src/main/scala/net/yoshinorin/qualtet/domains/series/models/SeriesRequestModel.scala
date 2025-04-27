package net.yoshinorin.qualtet.domains.series

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.Request
import net.yoshinorin.qualtet.domains.errors.{SeriesPathRequired, SeriesTitleRequired}
import net.yoshinorin.qualtet.syntax.*

final case class SeriesRequestModel(
  path: SeriesPath,
  title: String,
  description: Option[String]
) extends Request[SeriesRequestModel] {
  def postDecode: SeriesRequestModel = {
    // TODO: improve
    path.value.trimOrThrow(SeriesPathRequired(detail = "path is required"))
    new SeriesRequestModel(
      path = path,
      title = title.trimOrThrow(SeriesTitleRequired(detail = "title is required")),
      description = description
    )
  }
}

object SeriesRequestModel {
  given codecRequestSeries: JsonValueCodec[SeriesRequestModel] = JsonCodecMaker.make
  given codecRequestListSeries: JsonValueCodec[List[SeriesRequestModel]] = JsonCodecMaker.make

  def apply(path: SeriesPath, title: String, description: Option[String]): SeriesRequestModel = {
    // TODO: improve
    path.value.trimOrThrow(SeriesPathRequired(detail = "path is required"))
    new SeriesRequestModel(
      path = path,
      title = title.trimOrThrow(SeriesTitleRequired(detail = "title is required")),
      description = description
    )
  }
}
