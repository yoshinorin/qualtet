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
  def route: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ POST -> Root => {
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
    }.handleErrorWith {
      // TODO: DRY & Logging
      case f: Fail => f match {
        case Fail.NotFound(message) => NotFound(message)
        // TODO: `forbidden` to `unauthorized`
        case Fail.Unauthorized(message) => Forbidden(message)
        case Fail.UnprocessableEntity(message) => UnprocessableEntity(message)
        case Fail.BadRequest(message) => BadRequest(message)
        case Fail.Forbidden(message) => Forbidden(message)
        case Fail.InternalServerError(message) => InternalServerError(message)
      }
      case _ => InternalServerError("Internal Server Error")
    }
  }

}
