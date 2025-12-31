package net.yoshinorin.qualtet.http.routes.v1

import cats.data.EitherT
import cats.effect.*
import cats.Monad
import cats.implicits.*
import org.http4s.headers.Allow
import org.http4s.{AuthedRoutes, HttpRoutes, Request, Response}
import org.http4s.dsl.io.*
import org.http4s.ContextRequest
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.domains.series.{Series, SeriesId, SeriesPath, SeriesRequestModel, SeriesService}
import net.yoshinorin.qualtet.http.AuthProvider
import net.yoshinorin.qualtet.http.request.Decoder
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

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
      this.get.handleErrorWith(_.logWithStackTrace[IO].asResponse)
    case request @ GET -> Root / name =>
      implicit val r = request
      this.get(name).handleErrorWith(_.logWithStackTrace[IO].asResponse)
  }

  private[http] def seriesWithAuthed: AuthedRoutes[(AuthorResponseModel, String), IO] = AuthedRoutes.of { ctxRequest =>
    implicit val x = ctxRequest.req
    (ctxRequest match {
      case ContextRequest(_, r) =>
        r match {
          case request @ POST -> Root => this.post(ctxRequest.context)
          case request @ DELETE -> Root / nameOrId => this.delete(nameOrId)
          case request @ _ => MethodNotAllowed(Allow(Set(GET, POST)))
        }
    }).handleErrorWith(_.logWithStackTrace[IO].asResponse)
  }

  private[http] def post(payload: (AuthorResponseModel, String)): Request[IO] ?=> IO[Response[IO]] = {
    (for {
      decodedSeries <- EitherT(decode[SeriesRequestModel](payload._2))
      createdSeries <- EitherT(seriesService.create(decodedSeries))
    } yield createdSeries).value.flatMap {
      case Right(series) => series.asResponse(Created)
      case Left(error: DomainError) => error.asResponse
    }
  }

  // series
  private[http] def get: IO[Response[IO]] = {
    (for {
      series <- seriesService.getAll
      response <- series.asResponse(Ok)
    } yield response)
  }

  private[http] def get(name: String): Request[IO] ?=> IO[Response[IO]] = {
    (for {
      seriesPath <- EitherT.fromEither[IO](SeriesPath(name))
      seriesWithArticles <- EitherT(seriesService.get(seriesPath))
    } yield seriesWithArticles).value.flatMap {
      case Right(series) => series.asResponse(Ok)
      case Left(error: DomainError) => error.asResponse
    }
  }

  private[http] def delete(nameOrId: String): Request[IO] ?=> IO[Response[IO]] = {
    (for {
      _ <- EitherT(seriesService.delete(SeriesId(nameOrId)))
    } yield ()).value.flatMap {
      case Right(_) => logger.info(s"deleted series: ${nameOrId}") *> NoContent()
      case Left(error: DomainError) => error.asResponse
    }
  }

}
