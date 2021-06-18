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

  val random = new Random

  def apply(path: String, title: String, content: String, publishedAt: Long, updatedAt: Long): ResponseArticle = {
    new ResponseArticle(
      path,
      title,
      content.stripHtmlTags.substring(random.nextInt(70), 100),
      publishedAt,
      updatedAt
    )
  }
}
