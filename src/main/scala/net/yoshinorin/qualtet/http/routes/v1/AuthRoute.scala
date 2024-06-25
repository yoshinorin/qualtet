package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.Monad
import org.http4s.headers.{Allow, `Content-Type`, `WWW-Authenticate`}
import org.http4s.{Challenge, HttpRoutes, MediaType, Request, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.auth.{AuthService, RequestToken}
import net.yoshinorin.qualtet.http.RequestDecoder
import net.yoshinorin.qualtet.syntax.*

class AuthRoute[F[_]: Monad](authService: AuthService[F]) extends RequestDecoder {

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ POST -> Root => this.post(request)
      case request @ OPTIONS -> Root => NoContent() // TODO: return `Allow Header`
      case request @ _ => MethodNotAllowed(Allow(Set(POST)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  // token
  private[http] def post(request: Request[IO]): IO[Response[IO]] = {
    (for {
      stringifyRequest <- request.as[String]
      maybeRequestToken <- IO(decode[RequestToken](stringifyRequest))
    } yield maybeRequestToken).flatMap { mrt =>
      mrt match {
        case Left(_) => Unauthorized(`WWW-Authenticate`(Challenge("Bearer", "Unauthorized")))
        case Right(requestToken) =>
          authService.generateToken(requestToken).flatMap { t =>
            Ok(t.asJson, `Content-Type`(MediaType.application.json))
          }
      }
    }
  }

}
