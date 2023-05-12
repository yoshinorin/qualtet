package net.yoshinorin.qualtet.domains.series

import cats.effect.IO
import cats.Monad
import cats.implicits._
import net.yoshinorin.qualtet.actions.Action._
import net.yoshinorin.qualtet.actions.{Action, Continue}
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

  def findByNameActions(name: SeriesName): Action[Option[Series]] = {
    Continue(seriesRepository.findByName(name), Action.done[Option[Series]])
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
    this.findByName(data.name).flatMap {
      case Some(s: Series) =>
        for {
          _ <- transactor.transact(upsertActions(Series(s.id, s.name, data.title, data.description)))
          s <- this.findByName(data.name).throwIfNone(NotFound("series not found"))
        } yield s
      case None =>
        for {
          _ <- transactor.transact(upsertActions(Series(SeriesId(ULID.newULIDString.toLower), data.name, data.title, data.description)))
          s <- this.findByName(data.name).throwIfNone(NotFound("series not found"))
        } yield s
    }
  }

  /**
   * find a series by name
   *
   * @param name a series name
   * @return Series Instance
   */
  def findByName(name: SeriesName): IO[Option[Series]] = {
    transactor.transact(findByNameActions(name))
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
