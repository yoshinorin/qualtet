package net.yoshinorin.qualtet.http.routes

import cats.effect._
import cats.Monad
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.domains.series.{Series, SeriesName, RequestSeries}
import net.yoshinorin.qualtet.domains.series.SeriesService
import net.yoshinorin.qualtet.http.RequestDecoder
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.syntax._

class SeriesRoute[M[_]: Monad](
  seriesService: SeriesService[M]
) extends RequestDecoder {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def post(payload: (ResponseAuthor, String)): IO[Response[IO]] = {
    val maybeSeries = for {
      maybeSeries <- IO(decode[RequestSeries](payload._2))
    } yield maybeSeries

    maybeSeries.flatMap { s =>
      s match {
        case Left(f) => throw f
        case Right(s) =>
          seriesService.create(s).flatMap { createdSeries =>
            Created(createdSeries.asJson, `Content-Type`(MediaType.application.json))
          }
      }
    }.handleErrorWith(_.logWithStackTrace.andResponse)
  }

  // series
  def get: IO[Response[IO]] = {
    (for {
      series <- seriesService.getAll
      response <- Ok(series.asJson, `Content-Type`(MediaType.application.json))
    } yield response).handleErrorWith(_.logWithStackTrace.andResponse)
  }

  def get(name: String): IO[Response[IO]] = {
    (for {
      seriesWithArticles <- seriesService.get(SeriesName(name))
      response <- Ok(seriesWithArticles.asJson, `Content-Type`(MediaType.application.json))
    } yield response).handleErrorWith(_.logWithStackTrace.andResponse)
  }

}
