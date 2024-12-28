package net.yoshinorin.qualtet.domains.feeds

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.Path

// https://validator.w3.org/feed/docs/atom.html
final case class FeedResponseModel(
  title: String,
  link: Path,
  id: Path,
  published: Long,
  updated: Long
)
object FeedResponseModel {
  given codecResponseFeed: JsonValueCodec[FeedResponseModel] = JsonCodecMaker.make
  given codecResponseFeeds: JsonValueCodec[Seq[FeedResponseModel]] = JsonCodecMaker.make
}