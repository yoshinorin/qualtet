package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.*
import cats.Monad
import cats.implicits.*
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{AuthedRoutes, HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import org.http4s.ContextRequest
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.domains.series.{Series, SeriesName, SeriesRequestModel, SeriesService}
import net.yoshinorin.qualtet.http.AuthProvider
import net.yoshinorin.qualtet.http.request.Decoder
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory => Log4CatsLoggerFactory, SelfAwareStructuredLogger}

class SeriesRoute[F[_]: Monad](
  authProvider: AuthProvider[F],
  seriesService: SeriesService[F]
)(using loggerFactory: Log4CatsLoggerFactory[IO])
    extends Decoder[IO] {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  // NOTE: must be compose `auth route` after `Non auth route`.
  private[http] def index: HttpRoutes[IO] =
    (seriesWithoutAuth <+>
      authProvider.authenticate(seriesWithAuthed))

  private[http] def seriesWithoutAuth: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ GET -> Root =>
      implicit val r = request
      this.get.handleErrorWith(_.logWithStackTrace[IO].andResponse)
    case request @ GET -> Root / name =>
      implicit val r = request
      this.get(name).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  private[http] def seriesWithAuthed: AuthedRoutes[(AuthorResponseModel, String), IO] = AuthedRoutes.of { ctxRequest =>
    implicit val x = ctxRequest.req
    (ctxRequest match {
      case ContextRequest(_, r) =>
        r match {
          case request @ POST -> Root => this.post(ctxRequest.context)
          case request @ _ => MethodNotAllowed(Allow(Set(GET, POST, DELETE)))
        }
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  private[http] def post(payload: (AuthorResponseModel, String)): IO[Response[IO]] = {
    val maybeSeries = for {
      maybeSeries <- decode[SeriesRequestModel](payload._2)
    } yield maybeSeries

    maybeSeries.flatMap { s =>
      s match {
        case Left(f) => throw f
        case Right(s) =>
          seriesService.create(s).flatMap { createdSeries =>
            Created(createdSeries.asJson, `Content-Type`(MediaType.application.json))
          }
      }
    }
  }

  // series
  private[http] def get: IO[Response[IO]] = {
    (for {
      series <- seriesService.getAll
      response <- Ok(series.asJson, `Content-Type`(MediaType.application.json))
    } yield response)
  }

  private[http] def get(name: String): IO[Response[IO]] = {
    (for {
      seriesWithArticles <- seriesService.get(SeriesName(name))
      response <- Ok(seriesWithArticles.asJson, `Content-Type`(MediaType.application.json))
    } yield response)
  }

}
