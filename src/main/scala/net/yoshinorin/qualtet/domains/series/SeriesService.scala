package net.yoshinorin.qualtet.domains.series

import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.contentSerializing.ContentSerializingRepositoryAdapter
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.domains.errors.SeriesNotFound
import net.yoshinorin.qualtet.syntax.*
import wvlet.airframe.ulid.ULID

class SeriesService[F[_]: Monad](
  seriesRepositoryAdapter: SeriesRepositoryAdapter[F],
  contentSerializingRepositoryAdapter: ContentSerializingRepositoryAdapter[F],
  articleService: ArticleService[F]
)(using executer: Executer[F, IO]) {

  /**
   * create a series
   *
   * @param data Series
   * @return created Series
   */
  def create(data: SeriesRequestModel): IO[Series] = {
    for {
      _ <- this
        .findByName(data.name)
        .flatMap {
          case Some(s: Series) => executer.transact(seriesRepositoryAdapter.upsert(Series(s.id, s.name, s.path, data.title, data.description)))
          case None =>
            executer.transact(seriesRepositoryAdapter.upsert(Series(SeriesId(ULID.newULIDString.toLower), data.name, data.path, data.title, data.description)))
        }
      series <- this.findByName(data.name).errorIfNone(SeriesNotFound(detail = "series not found")).flatMap(_.liftTo[IO])
    } yield series
  }

  def findById(id: SeriesId): IO[Option[Series]] = {
    executer.transact(seriesRepositoryAdapter.findById(id))
  }

  def findByName(name: SeriesName): IO[Option[Series]] = {
    executer.transact(seriesRepositoryAdapter.findByName(name))
  }

  def findByPath(path: SeriesPath): IO[Option[Series]] = {
    executer.transact(seriesRepositoryAdapter.findByPath(path))
  }

  def findByContentId(id: ContentId): IO[Option[Series]] = {
    executer.transact(seriesRepositoryAdapter.findByContentId(id))
  }

  def get(path: SeriesPath): IO[SeriesResponseModel] = {
    for {
      series <- executer
        .transact(seriesRepositoryAdapter.findByPath(path))
        .errorIfNone(SeriesNotFound(detail = s"series not found: ${path.value}"))
        .flatMap(_.liftTo[IO])
      seriesWithArticles <- articleService.getBySeriesPath(series.path)
    } yield {
      SeriesResponseModel(series.id, series.name, series.path, series.title, series.description, seriesWithArticles.articles)
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

  def delete(id: SeriesId): IO[Unit] = {

    val queries = for {
      contentSerializingDelete <- executer.defer(contentSerializingRepositoryAdapter.deleteBySeriesId(id))
      seriesDelete <- executer.defer(seriesRepositoryAdapter.deleteBySeriesId(id))
    } yield (
      contentSerializingDelete,
      seriesDelete
    )

    for {
      _ <- this.findById(id).errorIfNone(SeriesNotFound(detail = s"series not found: ${id}")).flatMap(_.liftTo[IO])
      _ <- executer.transact2[Unit, Unit](queries)
    } yield ()
  }

}
