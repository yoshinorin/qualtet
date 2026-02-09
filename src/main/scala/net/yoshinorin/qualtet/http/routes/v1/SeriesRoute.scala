package net.yoshinorin.qualtet.http.routes.v1

import cats.data.EitherT
import cats.effect.Concurrent
import cats.Monad
import cats.implicits.*
import org.http4s.headers.Allow
import org.http4s.{AuthedRoutes, HttpRoutes, Request, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.ContextRequest
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.domains.series.{Series, SeriesId, SeriesPath, SeriesRequestModel, SeriesService}
import net.yoshinorin.qualtet.http.AuthProvider
import net.yoshinorin.qualtet.http.request.Decoder
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class SeriesRoute[F[_]: Concurrent, G[_]: Monad @nowarn](
  authProvider: AuthProvider[F, G],
  seriesService: SeriesService[F, G]
)(using loggerFactory: Log4CatsLoggerFactory[F])
    extends Decoder[F] {

  private given dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl.*

  given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  // NOTE: must be compose `auth route` after `Non auth route`.
  private[http] def index: HttpRoutes[F] =
    (seriesWithoutAuth <+>
      authProvider.authenticate(seriesWithAuthed))

  private[http] def seriesWithoutAuth: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ GET -> Root =>
      implicit val r = request
      this.get.handleErrorWith(_.logWithStackTrace[F].asResponse)
    case request @ GET -> Root / name =>
      implicit val r = request
      this.get(name).handleErrorWith(_.logWithStackTrace[F].asResponse)
  }

  private[http] def seriesWithAuthed: AuthedRoutes[(AuthorResponseModel, String), F] = AuthedRoutes.of { ctxRequest =>
    implicit val x = ctxRequest.req
    (ctxRequest match {
      case ContextRequest(_, r) =>
        r match {
          case request @ POST -> Root => this.post(ctxRequest.context)
          case request @ DELETE -> Root / nameOrId => this.delete(nameOrId)
          case request @ _ => MethodNotAllowed(Allow(Set(GET, POST)))
        }
    }).handleErrorWith(_.logWithStackTrace[F].asResponse)
  }

  private[http] def post(payload: (AuthorResponseModel, String)): Request[F] ?=> F[Response[F]] = {
    (for {
      decodedSeries <- EitherT(decode[SeriesRequestModel](payload._2))
      createdSeries <- EitherT(seriesService.create(decodedSeries))
    } yield createdSeries).value.flatMap {
      case Right(series) => series.asResponse(Created)
      case Left(error: DomainError) => error.asResponse
    }
  }

  // series
  private[http] def get: F[Response[F]] = {
    (for {
      series <- seriesService.getAll
      response <- series.asResponse(Ok)
    } yield response)
  }

  private[http] def get(name: String): Request[F] ?=> F[Response[F]] = {
    (for {
      seriesPath <- EitherT.fromEither[F](SeriesPath(name))
      seriesWithArticles <- EitherT(seriesService.get(seriesPath))
    } yield seriesWithArticles).value.flatMap {
      case Right(series) => series.asResponse(Ok)
      case Left(error: DomainError) => error.asResponse
    }
  }

  private[http] def delete(nameOrId: String): Request[F] ?=> F[Response[F]] = {
    (for {
      _ <- EitherT(seriesService.delete(SeriesId(nameOrId)))
    } yield ()).value.flatMap {
      case Right(_) => logger.info(s"deleted series: ${nameOrId}") *> NoContent()
      case Left(error: DomainError) => error.asResponse
    }
  }

}
