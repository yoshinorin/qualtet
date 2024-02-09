package net.yoshinorin.qualtet.domains.articles

import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.series.SeriesName
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.http.QueryParametersAliases.SqlParams

trait ArticleRepository[F[_]] {
  def getWithCount(contentTypeId: ContentTypeId, sqlParams: SqlParams): F[Seq[(Int, ResponseArticle)]]
  def findByTagNameWithCount(contentTypeId: ContentTypeId, tagName: TagName, sqlParams: SqlParams): F[Seq[(Int, ResponseArticle)]]
  def findBySeriesNameWithCount(contentTypeId: ContentTypeId, seriesName: SeriesName): F[Seq[(Int, ResponseArticle)]]
}
