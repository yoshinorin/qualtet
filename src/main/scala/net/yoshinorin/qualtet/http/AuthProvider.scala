package net.yoshinorin.qualtet.http

import cats.effect.IO
import cats.Monad
import cats.data.{Kleisli, OptionT}
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRoutes, Request, Response, Status}
import org.http4s.headers.Authorization
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.auth.AuthService
import net.yoshinorin.qualtet.domains.errors.{Error, NotFound, Unauthorized}
import net.yoshinorin.qualtet.syntax.*

class AuthProvider[F[_]: Monad](
  authService: AuthService[F]
) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  // TODO: cleanup messy code
  private def authUser: Kleisli[IO, Request[IO], Either[Error, (ResponseAuthor, String)]] =
    Kleisli({ request =>
      request.headers.get[Authorization].asEither[Error](Unauthorized("Authorization header is none")) match {
        case Left(f: Error) => IO(Left(f))
        case Right(auth: Authorization) =>
          val renderString = auth.credentials.renderString.replace("Bearer ", "").replace("bearer ", "")
          for {
            maybeAuthor <- authService.findAuthorFromJwtString(renderString)
            author <- IO(maybeAuthor.asEither[Error](NotFound(detail = "author not found")))
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

  private def onFailure: AuthedRoutes[Error, IO] = Kleisli { req =>
    OptionT.pure[IO](Response[IO](status = Status.Unauthorized))
  }

  def authenticate: AuthMiddleware[IO, (ResponseAuthor, String)] = AuthMiddleware(authUser, onFailure)
}
