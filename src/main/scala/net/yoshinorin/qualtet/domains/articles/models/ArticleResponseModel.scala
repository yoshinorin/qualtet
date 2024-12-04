package net.yoshinorin.qualtet.domains.articles

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.syntax.*

import scala.util.Random

final case class ArticleResponseModel(
  id: ContentId,
  path: Path,
  title: String,
  content: String,
  publishedAt: Long,
  updatedAt: Long
)

object ArticleResponseModel {
  given codecResponseArticle: JsonValueCodec[ArticleResponseModel] = JsonCodecMaker.make
  given codecResponseArticles: JsonValueCodec[Seq[ArticleResponseModel]] = JsonCodecMaker.make

  def apply(id: ContentId, path: Path, title: String, content: String, publishedAt: Long, updatedAt: Long): ArticleResponseModel = {
    val stripedContent = content.stripHtmlTags
    val stripedContentLen = if (stripedContent.length > 100) 100 else stripedContent.length
    new ArticleResponseModel(
      id,
      path,
      title,
      stripedContent.substring(0, Random.between((stripedContentLen - stripedContentLen / 3), stripedContentLen)),
      publishedAt,
      updatedAt
    )
  }
}

final case class ArticleWithCountResponseModel(
  count: Int,
  articles: Seq[ArticleResponseModel]
)

object ArticleWithCountResponseModel {
  given codecResponseArticleWithCount: JsonValueCodec[ArticleWithCountResponseModel] = JsonCodecMaker.make
  given codecResponseArticlesWithCount: JsonValueCodec[Seq[ArticleWithCountResponseModel]] = JsonCodecMaker.make
}
