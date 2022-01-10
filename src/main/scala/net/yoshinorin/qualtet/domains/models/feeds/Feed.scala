package net.yoshinorin.qualtet.domains.models.feeds

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import net.yoshinorin.qualtet.domains.models.contents.Path

final case class ResponseFeed(
  title: String,
  link: Path,
  id: Path,
  published: Long,
  updated: Long
)
object ResponseFeed {
  implicit val encodeResponseFeed: Encoder[ResponseFeed] = deriveEncoder[ResponseFeed]
  implicit val encodeResponseFeeds: Encoder[List[ResponseFeed]] = Encoder.encodeList[ResponseFeed]
  implicit val decodeResponseFeed: Decoder[ResponseFeed] = deriveDecoder[ResponseFeed]
  implicit val decodeResponseFeeds: Decoder[List[ResponseFeed]] = Decoder.decodeList[ResponseFeed]
}
