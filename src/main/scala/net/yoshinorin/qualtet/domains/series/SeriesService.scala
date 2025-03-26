package net.yoshinorin.qualtet.domains.series

import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.domains.errors.SeriesNotFound
import net.yoshinorin.qualtet.syntax.*
import wvlet.airframe.ulid.ULID

class SeriesService[F[_]: Monad](
  seriesRepositoryAdapter: SeriesRepositoryAdapter[F],
  articleService: ArticleService[F]
)(using executer: Executer[F, IO]) {

  /**
   * create a series
   *
   * @param data Series
   * @return created Series
   */
  def create(data: SeriesRequestModel): IO[Series] = {
    this
      .findByName(data.name)
      .flatMap {
        case Some(s: Series) => executer.transact(seriesRepositoryAdapter.upsert(Series(s.id, s.name, data.title, data.description)))
        case None => executer.transact(seriesRepositoryAdapter.upsert(Series(SeriesId(ULID.newULIDString.toLower), data.name, data.title, data.description)))
      }
      .flatMap { s =>
        this.findByName(data.name).throwIfNone(SeriesNotFound(detail = "series not found"))
      }
  }

  /**
   * find a series by name
   *
   * @param name a series name
   * @return Series Instance
   */
  def findByName(name: SeriesName): IO[Option[Series]] = {
    executer.transact(seriesRepositoryAdapter.findByName(name))
  }

  def findByContentId(id: ContentId): IO[Option[Series]] = {
    executer.transact(seriesRepositoryAdapter.findByContentId(id))
  }

  def get(name: SeriesName): IO[SeriesResponseModel] = {
    for {
      series <- executer.transact(seriesRepositoryAdapter.findByName(name)).throwIfNone(SeriesNotFound(detail = s"series not found: ${name.value}"))
      seriesWithArticles <- articleService.getBySeriesName(series.name)
    } yield {
      SeriesResponseModel(series.id, series.name, series.title, series.description, seriesWithArticles.articles)
    }
  }

  /**
   * get all series
   *
   * @return Series
   */
  def getAll: IO[Seq[Series]] = {
    executer.transact(seriesRepositoryAdapter.fetch)
  }

}
