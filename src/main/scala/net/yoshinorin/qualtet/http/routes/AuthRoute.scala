package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.{`Content-Type`, `WWW-Authenticate`}
import org.http4s.Challenge
import net.yoshinorin.qualtet.auth.{AuthService, RequestToken}
import net.yoshinorin.qualtet.http.RequestDecoder
import net.yoshinorin.qualtet.syntax.*

class AuthRoute[M[_]: Monad](authService: AuthService[M]) extends RequestDecoder {

  // token
  def post(request: Request[IO]): IO[Response[IO]] = {
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
    }.handleErrorWith(_.logWithStackTrace.andResponse)
  }

}
