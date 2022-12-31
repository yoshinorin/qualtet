package net.yoshinorin.qualtet.domains.search

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.domains.contents.{ContentId, Path}
import net.yoshinorin.qualtet.syntax._

import scala.util.Random

final case class ResponseSearch(
  path: Path,
  title: String,
  content: String,
  publishedAt: Long,
  updatedAt: Long
)

object ResponseSearch {
  implicit val codecResponseSearch: JsonValueCodec[ResponseSearch] = JsonCodecMaker.make
  implicit val codecResponseSearchs: JsonValueCodec[Seq[ResponseSearch]] = JsonCodecMaker.make

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
  implicit val codecResponseSearchWithCount: JsonValueCodec[ResponseSearchWithCount] = JsonCodecMaker.make
  implicit val codecResponseSearchsWithCount: JsonValueCodec[Seq[ResponseSearchWithCount]] = JsonCodecMaker.make
}
