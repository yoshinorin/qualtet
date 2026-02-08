package net.yoshinorin.qualtet.domains.series

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

class SeriesService[G[_]: Monad, F[_]: Monad](
  seriesRepositoryAdapter: SeriesRepositoryAdapter[G],
  contentSerializingRepositoryAdapter: ContentSerializingRepositoryAdapter[G],
  articleService: ArticleService[G, F]
)(using executer: Executer[G, F], loggerFactory: Log4CatsLoggerFactory[F]) {

  private given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  /**
   * create a series
   *
   * @param data Series
   * @return created Series
   */
  def create(data: SeriesRequestModel): F[Either[DomainError, Series]] = {
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
        case Some(series) => Monad[F].pure(Right(series))
        case None => Left(SeriesNotFound(detail = "series not found")).logLeft[F](Warn)
      }
    } yield result
  }

  def findById(id: SeriesId): F[Option[Series]] = {
    executer.transact(seriesRepositoryAdapter.findById(id))
  }

  def findByName(name: SeriesName): F[Option[Series]] = {
    executer.transact(seriesRepositoryAdapter.findByName(name))
  }

  def findByPath(path: SeriesPath): F[Option[Series]] = {
    executer.transact(seriesRepositoryAdapter.findByPath(path))
  }

  def findByContentId(id: ContentId): F[Option[Series]] = {
    executer.transact(seriesRepositoryAdapter.findByContentId(id))
  }

  def get(path: SeriesPath): F[Either[DomainError, SeriesResponseModel]] = {
    for {
      maybeSeries <- executer.transact(seriesRepositoryAdapter.findByPath(path))
      result <- maybeSeries match {
        case Some(series) =>
          articleService.getBySeriesPath(series.path).flatMap {
            case Right(seriesWithArticles) =>
              Monad[F].pure(Right(SeriesResponseModel(series.id, series.name, series.path, series.title, series.description, seriesWithArticles.articles)))
            case Left(error) =>
              Left(error).logLeft[F](Warn)
          }
        case None =>
          Left(SeriesNotFound(detail = s"series not found: ${path.value}")).logLeft[F](Warn)
      }
    } yield result
  }

  /**
   * get all series
   *
   * @return Series
   */
  def getAll: F[Seq[Series]] = {
    executer.transact(seriesRepositoryAdapter.fetch)
  }

  def delete(id: SeriesId): F[Either[DomainError, Unit]] = {

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
          Left(SeriesNotFound(detail = s"series not found: ${id}")).logLeft[F](Warn)
      }
    } yield result
  }

}
