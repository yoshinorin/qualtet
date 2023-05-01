package net.yoshinorin.qualtet.domains.series

import cats.effect.IO
import cats.Monad
import cats.implicits._
import net.yoshinorin.qualtet.actions.Action._
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.domains.contents.Path
import net.yoshinorin.qualtet.infrastructure.db.Transactor
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.syntax._
import wvlet.airframe.ulid.ULID

class SeriesService[M[_]: Monad](
  seriesRepository: SeriesRepository[M]
)(using transactor: Transactor[M]) {

  def upsertActions(data: Series): Action[Int] = {
    Continue(seriesRepository.upsert(data), Action.done[Int])
  }

  def findByPathActions(path: Path): Action[Option[Series]] = {
    Continue(seriesRepository.findByPath(path), Action.done[Option[Series]])
  }

  def fetchActions: Action[Seq[Series]] = {
    Continue(seriesRepository.getAll(), Action.done[Seq[Series]])
  }

  /**
   * create a series
   *
   * @param data Series
   * @return created Series
   */
  def create(data: RequestSeries): IO[Series] = {
    this.findByPath(data.path).flatMap {
      case Some(s: Series) =>
        for {
          _ <- transactor.transact(upsertActions(Series(s.id, s.path, data.title, data.description)))
          s <- this.findByPath(data.path).throwIfNone(NotFound("series not found"))
        } yield s
      case None =>
        for {
          _ <- transactor.transact(upsertActions(Series(SeriesId(ULID.newULIDString.toLower), data.path, data.title, data.description)))
          s <- this.findByPath(data.path).throwIfNone(NotFound("series not found"))
        } yield s
    }
  }

  /**
   * find a series by path
   *
   * @param path a series path
   * @return Series Instance
   */
  def findByPath(path: Path): IO[Option[Series]] = {
    transactor.transact(findByPathActions(path))
  }

  /**
   * get all series
   *
   * @return Series
   */
  def getAll: IO[Seq[Series]] = {
    transactor.transact(fetchActions)
  }

}
