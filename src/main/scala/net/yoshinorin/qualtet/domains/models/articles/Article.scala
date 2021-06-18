package net.yoshinorin.qualtet.domains.models.articles

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ResponseArticle(
  path: String,
  title: String,
  content: String,
  publishedAt: Long,
  updatedAt: Long
)

object ResponseArticle {
  implicit val encodeContent: Encoder[ResponseArticle] = deriveEncoder[ResponseArticle]
  implicit val encodeContents: Encoder[List[ResponseArticle]] = Encoder.encodeList[ResponseArticle]
  implicit val decodeContent: Decoder[ResponseArticle] = deriveDecoder[ResponseArticle]
  implicit val decodeContents: Decoder[List[ResponseArticle]] = Decoder.decodeList[ResponseArticle]
}
