package net.yoshinorin.qualtet.http

import akka.http.scaladsl.model.headers.{HttpChallenge, OAuth2BearerToken}
import akka.http.scaladsl.server.Directives.authenticateOrRejectWithChallenge
import akka.http.scaladsl.server.directives.{AuthenticationDirective, AuthenticationResult}
import cats.effect.IO
import net.yoshinorin.qualtet.auth.AuthService
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.message.Fail.NotFound

import scala.concurrent.Future

class Authentication(authService: AuthService) {

  def authenticate: AuthenticationDirective[ResponseAuthor] = {

    def findAuthor(token: String): IO[ResponseAuthor] = authService.findAuthorFromJwtString(token).flatMap {
      case Some(x: ResponseAuthor) => IO(x)
      case None => IO.raiseError(NotFound("author not found"))
    }

    // TODO: fix status code when reject
    // TODO: logging (when success & failure)
    authenticateOrRejectWithChallenge[OAuth2BearerToken, ResponseAuthor] {
      case Some(OAuth2BearerToken(token)) =>
        (for {
          x <- findAuthor(token)
        } yield AuthenticationResult.success(x)).unsafeToFuture()
      case _ =>
        Future.successful(AuthenticationResult.failWithChallenge(HttpChallenge("Bearer", None, Map("error" -> "Requires authentication"))))
    }

  }

}
