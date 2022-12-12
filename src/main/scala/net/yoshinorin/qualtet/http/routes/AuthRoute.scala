package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.auth.{AuthService, RequestToken}
import net.yoshinorin.qualtet.http.{RequestDecoder, ResponseHandler}
import net.yoshinorin.qualtet.syntax._

class AuthRoute(authService: AuthService) extends RequestDecoder with ResponseHandler {

  // token
  def post(request: Request[IO]) = {
    val maybeRequestToken = for {
      stringifyRequest <- request.as[String]
      maybeRequestToken <- IO(decode[RequestToken](stringifyRequest))
    } yield maybeRequestToken

    maybeRequestToken.flatMap { requestToken =>
      requestToken match {
        // TODO: `Forbidden` to `Unauthorized`
        case Left(_) => Forbidden("Forbidden")
        case Right(token) =>
          authService.generateToken(token).flatMap { t =>
            Ok(t.asJson, `Content-Type`(MediaType.application.json))
          }
      }
    }
  }

}
