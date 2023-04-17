package net.yoshinorin.qualtet.domains.articles

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.domains.contents.{ContentId, Path}
import net.yoshinorin.qualtet.syntax._

import scala.util.Random

final case class ResponseArticle(
  id: ContentId,
  path: Path,
  title: String,
  content: String,
  publishedAt: Long,
  updatedAt: Long
)

object ResponseArticle {
  given codecResponseArticle: JsonValueCodec[ResponseArticle] = JsonCodecMaker.make
  given codecResponseArticles: JsonValueCodec[Seq[ResponseArticle]] = JsonCodecMaker.make

  def apply(id: ContentId, path: Path, title: String, content: String, publishedAt: Long, updatedAt: Long): ResponseArticle = {
    val stripedContent = content.stripHtmlTags
    val stripedContentLen = if (stripedContent.length > 100) 100 else stripedContent.length
    new ResponseArticle(
      id,
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
  given codecResponseArticleWithCount: JsonValueCodec[ResponseArticleWithCount] = JsonCodecMaker.make
  given codecResponseArticlesWithCount: JsonValueCodec[Seq[ResponseArticleWithCount]] = JsonCodecMaker.make
}
