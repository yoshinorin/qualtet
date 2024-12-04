package net.yoshinorin.qualtet.domains.archives

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.Path

final case class ArchiveResponseModel(
  path: Path,
  title: String,
  publishedAt: Long
)

object ArchiveResponseModel {
  given codecContent: JsonValueCodec[ArchiveResponseModel] = JsonCodecMaker.make
  given codecContents: JsonValueCodec[Seq[ArchiveResponseModel]] = JsonCodecMaker.make
}
