package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.Concurrent
import cats.implicits.*
import cats.Monad
import org.http4s.headers.{Allow, `WWW-Authenticate`}
import org.http4s.{Challenge, HttpRoutes, Request, Response}
import org.http4s.dsl.Http4sDsl
import net.yoshinorin.qualtet.auth.{AuthService, RequestToken}
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.http.request.Decoder
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class AuthRoute[F[_]: Concurrent, G[_]: Monad @nowarn](authService: AuthService[F, G])(using loggerFactory: Log4CatsLoggerFactory[F]) extends Decoder[F] {

  private given dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl.*

  given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[F] = HttpRoutes.of[F] { implicit r =>
    (r match {
      case request @ POST -> Root => this.post(request)
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(POST)))
    }).handleErrorWith(_.logWithStackTrace[F].asResponse)
  }

  // token
  private[http] def post(request: Request[F]): Request[F] ?=> F[Response[F]] = {
    (for {
      stringifyRequest <- request.as[String]
      maybeRequestToken <- decode[RequestToken](stringifyRequest)
    } yield maybeRequestToken).flatMap { mrt =>
      mrt match {
        case Left(_) => Unauthorized(`WWW-Authenticate`(Challenge("Bearer", "Unauthorized")))
        case Right(requestToken) =>
          authService.generateToken(requestToken).flatMap {
            case Right(token) => token.asResponse(Ok)
            case Left(error: DomainError) => error.asResponse
          }
      }
    }
  }

}
