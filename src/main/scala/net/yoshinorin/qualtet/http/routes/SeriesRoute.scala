package net.yoshinorin.qualtet.http.routes

import cats.effect.*
import cats.Monad
import cats.implicits.*
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{AuthedRoutes, HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.domains.series.{Series, SeriesName, SeriesService, RequestSeries}
import net.yoshinorin.qualtet.http.{AuthProvider, MethodNotAllowedSupport, RequestDecoder}
import net.yoshinorin.qualtet.syntax.*

class SeriesRoute[M[_]: Monad](
  authProvider: AuthProvider[M],
  seriesService: SeriesService[M]
) extends RequestDecoder
    with MethodNotAllowedSupport {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  // NOTE: must be compose `auth route` after `Non auth route`.
  private[http] def index: HttpRoutes[IO] =
    seriesWithoutAuth <+>
      authProvider.authenticate(seriesWithAuthed)

  private[http] def seriesWithoutAuth: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => this.get
    case request @ GET -> Root / name => this.get(name)
  }

  private[http] def seriesWithAuthed: AuthedRoutes[(ResponseAuthor, String), IO] = AuthedRoutes.of {
    case request @ POST -> Root as payload => this.post(payload)
    case request @ _ =>
      methodNotAllowed(request.req, Allow(Set(GET, POST, DELETE)))
  }

  private[http] def post(payload: (ResponseAuthor, String)): IO[Response[IO]] = {
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
  private[http] def get: IO[Response[IO]] = {
    (for {
      series <- seriesService.getAll
      response <- Ok(series.asJson, `Content-Type`(MediaType.application.json))
    } yield response).handleErrorWith(_.logWithStackTrace.andResponse)
  }

  private[http] def get(name: String): IO[Response[IO]] = {
    (for {
      seriesWithArticles <- seriesService.get(SeriesName(name))
      response <- Ok(seriesWithArticles.asJson, `Content-Type`(MediaType.application.json))
    } yield response).handleErrorWith(_.logWithStackTrace.andResponse)
  }

}
