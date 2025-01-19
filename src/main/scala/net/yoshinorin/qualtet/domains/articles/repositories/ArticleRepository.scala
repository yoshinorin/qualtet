package net.yoshinorin.qualtet.domains.articles

import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.series.SeriesName
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.http.request.query.QueryParametersAliases.SqlParams

trait ArticleRepository[F[_]] {
  def getWithCount(contentTypeId: ContentTypeId, sqlParams: SqlParams): F[Seq[(Int, ArticleReadModel)]]
  def findByTagNameWithCount(contentTypeId: ContentTypeId, tagName: TagName, sqlParams: SqlParams): F[Seq[(Int, ArticleReadModel)]]
  def findBySeriesNameWithCount(contentTypeId: ContentTypeId, seriesName: SeriesName): F[Seq[(Int, ArticleReadModel)]]
}

object ArticleRepository {

  import doobie.Read
  import doobie.ConnectionIO
  import net.yoshinorin.qualtet.domains.contents.ContentId

  given ArticleRepository: ArticleRepository[ConnectionIO] = {
    new ArticleRepository[ConnectionIO] {
      given articlesWithCountRead: Read[(Int, ArticleReadModel)] =
        Read[(Int, (String, String, String, String, Long, Long))].map { case (cnt, (id, path, title, content, publishedAt, updatedAt)) =>
          (cnt, ArticleReadModel(ContentId(id), Path(path), title, content, publishedAt, updatedAt))
        }

      override def getWithCount(contentTypeId: ContentTypeId, sqlParams: SqlParams): ConnectionIO[Seq[(Int, ArticleReadModel)]] = {
        ArticleQuery.getWithCount(contentTypeId, sqlParams).to[Seq]
      }
      override def findByTagNameWithCount(contentTypeId: ContentTypeId, tagName: TagName, sqlParams: SqlParams): ConnectionIO[Seq[(Int, ArticleReadModel)]] = {
        ArticleQuery.findByTagNameWithCount(contentTypeId, tagName, sqlParams).to[Seq]
      }

      override def findBySeriesNameWithCount(contentTypeId: ContentTypeId, seriesName: SeriesName): ConnectionIO[Seq[(Int, ArticleReadModel)]] = {
        ArticleQuery.findBySeriesNameWithCount(contentTypeId, seriesName).to[Seq]
      }
    }
  }

}
