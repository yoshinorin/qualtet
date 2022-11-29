package net.yoshinorin.qualtet.http

import akka.http.scaladsl.model.headers.{HttpChallenge, OAuth2BearerToken}
import akka.http.scaladsl.server.Directives.authenticateOrRejectWithChallenge
import akka.http.scaladsl.server.directives.{AuthenticationDirective, AuthenticationResult}
import net.yoshinorin.qualtet.auth.AuthService
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.syntax._

import cats.effect.unsafe.implicits.global
import scala.concurrent.Future

// TODO: delete all
class Authentication(authService: AuthService) {

  def authenticate: AuthenticationDirective[ResponseAuthor] = {

    // TODO: fix status code when reject
    // TODO: logging (when success & failure)
    authenticateOrRejectWithChallenge[OAuth2BearerToken, ResponseAuthor] {
      case Some(OAuth2BearerToken(token)) =>
        (for {
          x <- authService.findAuthorFromJwtString(token).throwIfNone(NotFound("author not found"))
        } yield AuthenticationResult.success(x)).unsafeToFuture()
      case _ =>
        Future.successful(AuthenticationResult.failWithChallenge(HttpChallenge("Bearer", None, Map("error" -> "Requires authentication"))))
    }

  }

}
