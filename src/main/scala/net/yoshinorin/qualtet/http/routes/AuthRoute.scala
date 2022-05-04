package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import net.yoshinorin.qualtet.auth.{AuthService, RequestToken, ResponseToken}
import net.yoshinorin.qualtet.error.Fail
import net.yoshinorin.qualtet.http.{RequestDecoder, ResponseHandler}

class AuthRoute(authService: AuthService) extends RequestDecoder with ResponseHandler {

  def route: Route = {
    pathPrefix("token") {
      pathEndOrSingleSlash {
        post {
          entity(as[String]) { payload =>
            decode[RequestToken](payload) match {
              case Right(requestToken) =>
                onSuccess(
                  authService
                    .generateToken(requestToken)
                    .handleErrorWith { e => IO.pure(e) }
                    .unsafeToFuture()
                ) {
                  case r: ResponseToken =>
                    httpResponse(Created, r)
                  case f: Fail =>
                    httpResponse(f)
                  case _ =>
                    httpResponse(Fail.InternalServerError("Internal server error"))
                }
              case Left(message) =>
                httpResponse(message)
            }
          }
        }
      }
    }
  }

}
