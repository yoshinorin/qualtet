package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.auth.{AuthService, RequestToken, ResponseToken}
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.http.{RequestDecoder, ResponseHandler}
import net.yoshinorin.qualtet.syntax._

class AuthRoute(authService: AuthService) extends RequestDecoder with ResponseHandler {

  // token
  def route: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ POST -> Root => {
      for {
        stringifyRequest <- request.as[String]
        maybeRequestToken <- decode[RequestToken](stringifyRequest) match {
          case Left(value) =>
            println(stringifyRequest)
            Ok("invalid")
          // case Right(token) => Ok(authService.generateToken(token), `Content-Type`(MediaType.application.json))
          case Right(token) => Ok("ok")
        }
      } yield maybeRequestToken
    }
  }

}
