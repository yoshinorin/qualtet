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

final case class ResponseArticleSimple(
  path: String,
  title: String,
  publishedAt: Long
)

object ResponseArticleSimple {
  implicit val encodeContent: Encoder[ResponseArticleSimple] = deriveEncoder[ResponseArticleSimple]
  implicit val encodeContents: Encoder[List[ResponseArticleSimple]] = Encoder.encodeList[ResponseArticleSimple]
  implicit val decodeContent: Decoder[ResponseArticleSimple] = deriveDecoder[ResponseArticleSimple]
  implicit val decodeContents: Decoder[List[ResponseArticleSimple]] = Decoder.decodeList[ResponseArticleSimple]
}
