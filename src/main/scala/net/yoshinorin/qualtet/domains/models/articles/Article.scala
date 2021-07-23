package net.yoshinorin.qualtet.domains.models.articles

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import net.yoshinorin.qualtet.utils.StringOps.StringOps

import scala.util.Random

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

  def apply(path: String, title: String, content: String, publishedAt: Long, updatedAt: Long): ResponseArticle = {
    val stripedContent = content.stripHtmlTags
    val stripedContentLen = if (stripedContent.length > 100) 100 else stripedContent.length
    new ResponseArticle(
      path,
      title,
      stripedContent.substring(0, Random.between((stripedContentLen - stripedContentLen / 3), stripedContentLen)),
      publishedAt,
      updatedAt
    )
  }
}
