package net.yoshinorin.qualtet.http

import cats.effect.IO
import cats.Monad
import cats.data.Kleisli
import cats.data.OptionT
import org.http4s.server._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.Authorization
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.auth.AuthService
import net.yoshinorin.qualtet.message.Fail.Unauthorized
import net.yoshinorin.qualtet.syntax._

class AuthProvider[F[_]: Monad](
  authService: AuthService[F]
) {
  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  private def authUser: Kleisli[IO, Request[IO], Either[String, (ResponseAuthor, String)]] =
    Kleisli({ request =>
      for {
        auth <- IO(request.headers.get[Authorization].orThrow(Unauthorized("Authorization header is none")))
        author <- authService.findAuthorFromJwtString(auth.credentials.renderString.replace("Bearer ", ""))
        payload <- request.as[String]
      } yield author match {
        case None =>
          logger.error(s"Invalid author: ${author}")
          Left("Unauthorized")
        case Some(author) =>
          logger.info(s"Authorization succeeded: ${author}")
          Right((author, payload))
      }
    })

  private def onFailure: AuthedRoutes[String, IO] = Kleisli(req => OptionT.liftF(Forbidden(req.context)))

  def authenticate: AuthMiddleware[IO, (ResponseAuthor, String)] = AuthMiddleware(authUser, onFailure)
}
