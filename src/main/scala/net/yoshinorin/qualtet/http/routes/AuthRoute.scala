package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.auth.{AuthService, RequestToken}
import net.yoshinorin.qualtet.http.RequestDecoder
import net.yoshinorin.qualtet.syntax._

class AuthRoute(authService: AuthService) extends RequestDecoder {

  // token
  def post(request: Request[IO]): IO[Response[IO]] = {
    (for {
      stringifyRequest <- request.as[String]
      maybeRequestToken <- IO(decode[RequestToken](stringifyRequest))
    } yield maybeRequestToken).flatMap { mrt =>
      mrt match {
        // TODO: `Forbidden` to `Unauthorized`
        case Left(_) => Forbidden("Forbidden")
        case Right(requestToken) =>
          authService.generateToken(requestToken).flatMap { t =>
            Ok(t.asJson, `Content-Type`(MediaType.application.json))
          }
      }
    }
  }

}
