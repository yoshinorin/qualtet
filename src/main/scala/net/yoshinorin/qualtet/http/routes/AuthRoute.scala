package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import cats.effect.IO
import io.circe.syntax._
import net.yoshinorin.qualtet.auth.{AuthService, ReponseToken, RequestToken}
import net.yoshinorin.qualtet.domains.models.Fail
import net.yoshinorin.qualtet.http.RequestDecoder

class AuthRoute(authService: AuthService) extends RequestDecoder {

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
                  case rt: ReponseToken =>
                    complete(HttpResponse(Created, entity = HttpEntity(ContentTypes.`application/json`, s"${rt.asJson}")))
                  case f: Fail =>
                    complete(HttpResponse(UnprocessableEntity, entity = HttpEntity(ContentTypes.`application/json`, s"${f.asJson}")))
                  case _ =>
                    // TODO: create Internal server error case class
                    complete(HttpResponse(InternalServerError, entity = HttpEntity(ContentTypes.`application/json`, s"Internal server error")))
                }

              case Left(message) =>
                complete(HttpResponse(BadRequest, entity = HttpEntity(ContentTypes.`application/json`, s"${message.asJson}")))
            }
          }
        }
      }
    }
  }

}
