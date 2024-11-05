package net.yoshinorin.qualtet.domains.series

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.articles.ResponseArticle

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
