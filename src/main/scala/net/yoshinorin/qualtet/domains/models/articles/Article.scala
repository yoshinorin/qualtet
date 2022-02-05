package net.yoshinorin.qualtet.domains.models.articles

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import net.yoshinorin.qualtet.domains.models.authors.AuthorName
import net.yoshinorin.qualtet.domains.models.contents.Path
import net.yoshinorin.qualtet.utils.StringOps.StringOps

import scala.util.Random

final case class ResponseArticle(
  path: Path,
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

  def apply(path: Path, title: String, content: String, publishedAt: Long, updatedAt: Long): ResponseArticle = {
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

final case class ResponseArticleWithCount(
  count: Int,
  articles: Seq[ResponseArticle]
)

object ResponseArticleWithCount {
  implicit val encodeResponseArticleWithCount: Encoder[ResponseArticleWithCount] = deriveEncoder[ResponseArticleWithCount]
  implicit val encodeResponseArticlesWithCount: Encoder[List[ResponseArticleWithCount]] = Encoder.encodeList[ResponseArticleWithCount]
  implicit val decodeResponseArticleWithCount: Decoder[ResponseArticleWithCount] = deriveDecoder[ResponseArticleWithCount]
  implicit val decodeResponseArticlesWithCount: Decoder[List[ResponseArticleWithCount]] = Decoder.decodeList[ResponseArticleWithCount]
}
