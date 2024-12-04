package net.yoshinorin.qualtet.domains.search

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.syntax.*

final case class SearchResponseModel(
  path: Path,
  title: String,
  content: String,
  publishedAt: Long,
  updatedAt: Long
)

object SearchResponseModel {
  given codecResponseSearch: JsonValueCodec[SearchResponseModel] = JsonCodecMaker.make
  given codecResponseSearchs: JsonValueCodec[Seq[SearchResponseModel]] = JsonCodecMaker.make

  def apply(path: Path, title: String, content: String, publishedAt: Long, updatedAt: Long): SearchResponseModel = {
    val stripedContent = content.stripHtmlTags
    new SearchResponseModel(
      path,
      title,
      stripedContent,
      publishedAt,
      updatedAt
    )
  }
}

final case class SearchWithCountResponseModel(
  count: Int,
  contents: Seq[SearchResponseModel]
)

object SearchWithCountResponseModel {
  given codecResponseSearchWithCount: JsonValueCodec[SearchWithCountResponseModel] = JsonCodecMaker.make
  given codecResponseSearchsWithCount: JsonValueCodec[Seq[SearchWithCountResponseModel]] = JsonCodecMaker.make
}
