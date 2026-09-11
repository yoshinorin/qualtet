package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.domains.contents.ContentPath

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

final case class ArchiveResponseModel(
  path: ContentPath,
  title: String,
  publishedAt: Long
)

object ArchiveResponseModel {
  given codecContent: JsonValueCodec[ArchiveResponseModel] = JsonCodecMaker.make
  given codecContents: JsonValueCodec[Seq[ArchiveResponseModel]] = JsonCodecMaker.make
}
