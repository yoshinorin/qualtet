package net.yoshinorin.qualtet.domains.series

import cats.data.ContT
import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.domains.errors.SeriesNotFound
import net.yoshinorin.qualtet.syntax.*
import wvlet.airframe.ulid.ULID

class SeriesService[F[_]: Monad](
  seriesRepository: SeriesRepository[F],
  articleService: ArticleService[F]
)(using executer: Executer[F, IO]) {

  def upsertCont(data: Series): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
      val w = SeriesWriteModel(id = data.id, name = data.name, title = data.title, description = data.description)
      seriesRepository.upsert(w)
    }
  }

  def findByNameCont(name: SeriesName): ContT[F, Option[Series], Option[Series]] = {
    ContT.apply[F, Option[Series], Option[Series]] { next =>
      seriesRepository.findByName(name).map { x =>
        x.map { s =>
          Series(s.id, s.name, s.title, s.description)
        }
      }
    }
  }

  def fetchCont: ContT[F, Seq[Series], Seq[Series]] = {
    ContT.apply[F, Seq[Series], Seq[Series]] { next =>
      seriesRepository.getAll().map { x =>
        x.map { s =>
          Series(s.id, s.name, s.title, s.description)
        }
      }
    }
  }

  /**
   * create a series
   *
   * @param data Series
   * @return created Series
   */
  def create(data: RequestSeries): IO[Series] = {
    this
      .findByName(data.name)
      .flatMap {
        case Some(s: Series) => executer.transact(upsertCont(Series(s.id, s.name, data.title, data.description)))
        case None => executer.transact(upsertCont(Series(SeriesId(ULID.newULIDString.toLower), data.name, data.title, data.description)))
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
    executer.transact(findByNameCont(name))
  }

  def get(name: SeriesName): IO[ResponseSeries] = {
    for {
      series <- executer.transact(findByNameCont(name)).throwIfNone(SeriesNotFound(detail = s"series not found: ${name.value}"))
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
    executer.transact(fetchCont)
  }

}
