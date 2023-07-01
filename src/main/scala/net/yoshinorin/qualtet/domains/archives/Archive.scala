package net.yoshinorin.qualtet.domains.archives

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.contents.Path

final case class ResponseArchive(
  path: Path,
  title: String,
  publishedAt: Long
)

object ResponseArchive {
  given codecContent: JsonValueCodec[ResponseArchive] = JsonCodecMaker.make
  given codecContents: JsonValueCodec[Seq[ResponseArchive]] = JsonCodecMaker.make
}
