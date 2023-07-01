package net.yoshinorin.qualtet.domains.search

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.contents.{ContentId, Path}
import net.yoshinorin.qualtet.syntax.*

import scala.util.Random

final case class ResponseSearch(
  path: Path,
  title: String,
  content: String,
  publishedAt: Long,
  updatedAt: Long
)

object ResponseSearch {
  given codecResponseSearch: JsonValueCodec[ResponseSearch] = JsonCodecMaker.make
  given codecResponseSearchs: JsonValueCodec[Seq[ResponseSearch]] = JsonCodecMaker.make

  def apply(path: Path, title: String, content: String, publishedAt: Long, updatedAt: Long): ResponseSearch = {
    val stripedContent = content.stripHtmlTags
    new ResponseSearch(
      path,
      title,
      stripedContent,
      publishedAt,
      updatedAt
    )
  }
}

final case class ResponseSearchWithCount(
  count: Int,
  contents: Seq[ResponseSearch]
)

object ResponseSearchWithCount {
  given codecResponseSearchWithCount: JsonValueCodec[ResponseSearchWithCount] = JsonCodecMaker.make
  given codecResponseSearchsWithCount: JsonValueCodec[Seq[ResponseSearchWithCount]] = JsonCodecMaker.make
}
