package net.yoshinorin.qualtet.domains.series

import net.yoshinorin.qualtet.domains.articles.ArticleResponseModel

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

final case class SeriesResponseModel(
  id: SeriesId,
  name: SeriesName,
  path: SeriesPath,
  title: String,
  description: Option[String],
  articles: Seq[ArticleResponseModel]
)

object SeriesResponseModel {
  given codecResponseSeries: JsonValueCodec[SeriesResponseModel] = JsonCodecMaker.make
  given codecResponseSeriesWithCount: JsonValueCodec[Seq[SeriesResponseModel]] = JsonCodecMaker.make
}
