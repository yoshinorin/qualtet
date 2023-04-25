package net.yoshinorin.qualtet.domains.series

import cats.effect.IO
import cats.Monad
import cats.implicits._
import net.yoshinorin.qualtet.actions.Action._
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.domains.contents.Path
import net.yoshinorin.qualtet.infrastructure.db.Transactor
import net.yoshinorin.qualtet.syntax._

class SeriesService[M[_]: Monad](
  seriesRepository: SeriesRepository[M]
)(using transactor: Transactor[M]) {

  def findByPathActions(path: Path): Action[Option[Series]] = {
    Continue(seriesRepository.findByPath(path), Action.done[Option[Series]])
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

}
