package net.yoshinorin.qualtet.domains.series

import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.actions.Action.*
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.infrastructure.db.Transactor
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.syntax.*
import wvlet.airframe.ulid.ULID

class SeriesService[F[_]: Monad](
  seriesRepository: SeriesRepository[F],
  articleService: ArticleService[F]
)(using transactor: Transactor[F, IO]) {

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
          s <- this.findByName(data.name).throwIfNone(NotFound(detail = "series not found"))
        } yield s
      case None =>
        for {
          _ <- transactor.transact(upsertActions(Series(SeriesId(ULID.newULIDString.toLower), data.name, data.title, data.description)))
          s <- this.findByName(data.name).throwIfNone(NotFound(detail = "series not found"))
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

  def get(name: SeriesName): IO[ResponseSeries] = {
    for {
      series <- transactor.transact(findByNameActions(name)).throwIfNone(NotFound(detail = s"series not found: ${name.value}"))
      seriesWithArticles <- articleService.getBySeriesName(series.name)
    } yield {
      ResponseSeries(series.id, series.name, series.title, series.description, seriesWithArticles.articles)
    }
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
