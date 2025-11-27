package net.yoshinorin.qualtet.domains.series

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.Request
import net.yoshinorin.qualtet.domains.errors.{DomainError, SeriesNameRequired, SeriesPathRequired, SeriesTitleRequired}
import net.yoshinorin.qualtet.syntax.*

final case class SeriesRequestModel(
  name: SeriesName,
  path: SeriesPath,
  title: String,
  description: Option[String]
) extends Request[SeriesRequestModel] {
  def postDecode: Either[DomainError, SeriesRequestModel] = {
    for {
      _ <- name.value.trimOrError(SeriesNameRequired(detail = "name is required"))
      _ <- path.value.trimOrError(SeriesPathRequired(detail = "path is required"))
      decodedTitle <- title.trimOrError(SeriesTitleRequired(detail = "title is required"))
    } yield new SeriesRequestModel(
      name = name,
      path = path,
      title = decodedTitle,
      description = description
    )
  }
}

object SeriesRequestModel {
  given codecRequestSeries: JsonValueCodec[SeriesRequestModel] = JsonCodecMaker.make
  given codecRequestListSeries: JsonValueCodec[List[SeriesRequestModel]] = JsonCodecMaker.make

  def apply(name: SeriesName, path: SeriesPath, title: String, description: Option[String]): Either[DomainError, SeriesRequestModel] = {
    for {
      _ <- name.value.trimOrError(SeriesNameRequired(detail = "name is required"))
      _ <- path.value.trimOrError(SeriesPathRequired(detail = "path is required"))
      decodedTitle <- title.trimOrError(SeriesTitleRequired(detail = "title is required"))
    } yield new SeriesRequestModel(
      name = name,
      path = path,
      title = decodedTitle,
      description = description
    )
  }
}
