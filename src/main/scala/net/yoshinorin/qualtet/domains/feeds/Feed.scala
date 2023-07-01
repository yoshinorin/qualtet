package net.yoshinorin.qualtet.domains.feeds

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
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
  given codecResponseFeed: JsonValueCodec[ResponseFeed] = JsonCodecMaker.make
  given codecResponseFeeds: JsonValueCodec[Seq[ResponseFeed]] = JsonCodecMaker.make
}
