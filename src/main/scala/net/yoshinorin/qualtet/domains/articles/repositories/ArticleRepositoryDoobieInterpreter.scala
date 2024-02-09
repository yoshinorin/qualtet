package net.yoshinorin.qualtet.domains.articles

import doobie.Read
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.contents.{ContentId, Path}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.series.SeriesName
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.http.QueryParametersAliases.SqlParams

class ArticleRepositoryDoobieInterpreter extends ArticleRepository[ConnectionIO] {

  given responseArticleWithCountRead: Read[(Int, ResponseArticle)] =
    Read[(Int, (String, String, String, String, Long, Long))].map { case (cnt, (id, path, title, content, publishedAt, updatedAt)) =>
      (cnt, ResponseArticle(ContentId(id), Path(path), title, content, publishedAt, updatedAt))
    }

  override def getWithCount(contentTypeId: ContentTypeId, sqlParams: SqlParams): ConnectionIO[Seq[(Int, ResponseArticle)]] = {
    ArticleQuery.getWithCount(contentTypeId, sqlParams).to[Seq]
  }
  override def findByTagNameWithCount(contentTypeId: ContentTypeId, tagName: TagName, sqlParams: SqlParams): ConnectionIO[Seq[(Int, ResponseArticle)]] = {
    ArticleQuery.findByTagNameWithCount(contentTypeId, tagName, sqlParams).to[Seq]
  }

  override def findBySeriesNameWithCount(contentTypeId: ContentTypeId, seriesName: SeriesName): ConnectionIO[Seq[(Int, ResponseArticle)]] = {
    ArticleQuery.findBySeriesNameWithCount(contentTypeId, seriesName).to[Seq]
  }
}
