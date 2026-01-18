package net.yoshinorin.qualtet.domains.series

import cats.effect.IO
import cats.Monad
import cats.implicits.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.contentSerializing.ContentSerializingRepositoryAdapter
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.domains.errors.{DomainError, SeriesNotFound}
import net.yoshinorin.qualtet.syntax.*
import wvlet.airframe.ulid.ULID

class SeriesService[F[_]: Monad](
  seriesRepositoryAdapter: SeriesRepositoryAdapter[F],
  contentSerializingRepositoryAdapter: ContentSerializingRepositoryAdapter[F],
  articleService: ArticleService[F]
)(using executer: Executer[F, IO], loggerFactory: Log4CatsLoggerFactory[IO]) {

  private given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  /**
   * create a series
   *
   * @param data Series
   * @return created Series
   */
  def create(data: SeriesRequestModel): IO[Either[DomainError, Series]] = {
    for {
      _ <- this
        .findByName(data.name)
        .flatMap {
          case Some(s: Series) => executer.transact(seriesRepositoryAdapter.upsert(Series(s.id, s.name, s.path, data.title, data.description)))
          case None =>
            executer.transact(seriesRepositoryAdapter.upsert(Series(SeriesId(ULID.newULIDString.toLower), data.name, data.path, data.title, data.description)))
        }
      maybeSeries <- this.findByName(data.name)
      result <- maybeSeries match {
        case Some(series) => IO.pure(Right(series))
        case None => Left(SeriesNotFound(detail = "series not found")).logLeft[IO](Warn)
      }
    } yield result
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

  def get(path: SeriesPath): IO[Either[DomainError, SeriesResponseModel]] = {
    for {
      maybeSeries <- executer.transact(seriesRepositoryAdapter.findByPath(path))
      result <- maybeSeries match {
        case Some(series) =>
          articleService.getBySeriesPath(series.path).flatMap {
            case Right(seriesWithArticles) =>
              IO.pure(Right(SeriesResponseModel(series.id, series.name, series.path, series.title, series.description, seriesWithArticles.articles)))
            case Left(error) =>
              Left(error).logLeft[IO](Warn)
          }
        case None =>
          Left(SeriesNotFound(detail = s"series not found: ${path.value}")).logLeft[IO](Warn)
      }
    } yield result
  }

  /**
   * get all series
   *
   * @return Series
   */
  def getAll: IO[Seq[Series]] = {
    executer.transact(seriesRepositoryAdapter.fetch)
  }

  def delete(id: SeriesId): IO[Either[DomainError, Unit]] = {

    val queries = for {
      contentSerializingDelete <- executer.defer(contentSerializingRepositoryAdapter.deleteBySeriesId(id))
      seriesDelete <- executer.defer(seriesRepositoryAdapter.deleteBySeriesId(id))
    } yield (
      contentSerializingDelete,
      seriesDelete
    )

    for {
      maybeSeries <- this.findById(id)
      result <- maybeSeries match {
        case Some(_) =>
          executer.transact2[Unit, Unit](queries).map(_ => Right(()))
        case None =>
          Left(SeriesNotFound(detail = s"series not found: ${id}")).logLeft[IO](Warn)
      }
    } yield result
  }

}
