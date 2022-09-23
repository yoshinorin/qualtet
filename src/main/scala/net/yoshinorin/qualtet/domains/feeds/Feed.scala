package net.yoshinorin.qualtet.domains.feeds

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.domains.contents.Path

// https://validator.w3.org/feed/docs/atom.html
final case class ResponseFeed(
  title: String,
  link: Path,
  id: Path,
  published: Long,
  updated: Long
)
object ResponseFeed {
  implicit val codecResponseFeed: JsonValueCodec[ResponseFeed] = JsonCodecMaker.make
  implicit val codecResponseFeeds: JsonValueCodec[Seq[ResponseFeed]] = JsonCodecMaker.make
}
