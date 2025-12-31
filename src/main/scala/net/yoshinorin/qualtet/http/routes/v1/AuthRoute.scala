package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.Monad
import org.http4s.headers.{Allow, `WWW-Authenticate`}
import org.http4s.{Challenge, HttpRoutes, Request, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.auth.{AuthService, RequestToken}
import net.yoshinorin.qualtet.http.request.Decoder
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

class AuthRoute[F[_]: Monad](authService: AuthService[F])(using loggerFactory: Log4CatsLoggerFactory[IO]) extends Decoder[IO] {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ POST -> Root => this.post(request)
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(POST)))
    }).handleErrorWith(_.logWithStackTrace[IO].asResponse)
  }

  // token
  private[http] def post(request: Request[IO]): IO[Response[IO]] = {
    (for {
      stringifyRequest <- request.as[String]
      maybeRequestToken <- decode[RequestToken](stringifyRequest)
    } yield maybeRequestToken).flatMap { mrt =>
      mrt match {
        case Left(_) => Unauthorized(`WWW-Authenticate`(Challenge("Bearer", "Unauthorized")))
        case Right(requestToken) =>
          authService.generateToken(requestToken).flatMap(_.asResponse(Ok))
      }
    }
  }

}
