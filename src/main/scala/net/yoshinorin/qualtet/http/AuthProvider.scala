package net.yoshinorin.qualtet.http

import cats.effect.IO
import cats.Monad
import cats.data.Kleisli
import cats.data.OptionT
import org.http4s.server.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.Authorization
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.auth.AuthService
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.message.Fail.Unauthorized
import net.yoshinorin.qualtet.syntax.*

class AuthProvider[M[_]: Monad](
  authService: AuthService[M]
) {
  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  private def authUser: Kleisli[IO, Request[IO], Either[Fail, (ResponseAuthor, String)]] =
    Kleisli({ request =>
      for {
        auth <- IO(request.headers.get[Authorization].orThrow(Unauthorized("Authorization header is none")))
        author <- authService.findAuthorFromJwtString(auth.credentials.renderString.replace("Bearer ", ""))
        payload <- request.as[String]
      } yield author match {
        case None =>
          logger.error(s"Invalid author: ${author}")
          Left(Unauthorized("Unauthorized"))
        case Some(author) =>
          logger.info(s"Authorization succeeded: ${author}")
          Right((author, payload))
      }
    })

  private def onFailure: AuthedRoutes[Fail, IO] = Kleisli { req =>
    OptionT.pure[IO](Response[IO](status = Status.Unauthorized))
  }

  def authenticate: AuthMiddleware[IO, (ResponseAuthor, String)] = AuthMiddleware(authUser, onFailure)
}
