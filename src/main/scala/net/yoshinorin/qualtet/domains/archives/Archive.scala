package net.yoshinorin.qualtet.domains.archives

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.domains.contents.Path

final case class ResponseArchive(
  path: Path,
  title: String,
  publishedAt: Long
)

object ResponseArchive {
  implicit val codecContent: JsonValueCodec[ResponseArchive] = JsonCodecMaker.make
  implicit val codecContents: JsonValueCodec[Seq[ResponseArchive]] = JsonCodecMaker.make
}
