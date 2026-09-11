package net.yoshinorin.qualtet.domains.feeds

import net.yoshinorin.qualtet.domains.contents.ContentPath

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

// https://validator.w3.org/feed/docs/atom.html
final case class FeedResponseModel(
  title: String,
  link: ContentPath,
  id: ContentPath,
  published: Long,
  updated: Long
)
object FeedResponseModel {
  given codecResponseFeed: JsonValueCodec[FeedResponseModel] = JsonCodecMaker.make
  given codecResponseFeeds: JsonValueCodec[Seq[FeedResponseModel]] = JsonCodecMaker.make
}
