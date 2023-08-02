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
import org.typelevel.ci.*
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.auth.AuthService
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.message.Fail.{NotFound, Unauthorized}
import net.yoshinorin.qualtet.syntax.*

class AuthProvider[M[_]: Monad](
  authService: AuthService[M]
) {
  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  // TODO: cleanup messy code
  private def authUser: Kleisli[IO, Request[IO], Either[Fail, (ResponseAuthor, String)]] =
    Kleisli({ request =>

      val maybeRequestHeader = request.headers.get[Authorization].asEither[Fail](Unauthorized("Authorization header is none")) match {
        case Left(f: Fail) => Left(f)
        case Right(auth: Authorization) => Right(auth)
      }

      maybeRequestHeader match {
        case Left(fail) => IO(Left(fail))
        case Right(auth) =>
          val renderString = auth.credentials.renderString.replace("Bearer ", "").replace("bearer ", "")
          for {
            maybeAuthor <- authService.findAuthorFromJwtString(renderString)
            author <- IO(maybeAuthor.asEither[Fail](NotFound("author not found")))
            payload <- request.as[String]
          } yield {
            author match {
              case Left(fail) => {
                logger.error(fail.getMessage)
                Left(fail)
              }
              case Right(value) =>
                logger.info(s"Authorization succeeded: ${value}")
                Right((value, payload))
            }
          }
      }
    })

  private def onFailure: AuthedRoutes[Fail, IO] = Kleisli { req =>
    OptionT.pure[IO](Response[IO](status = Status.Unauthorized))
  }

  def authenticate: AuthMiddleware[IO, (ResponseAuthor, String)] = AuthMiddleware(authUser, onFailure)
}
