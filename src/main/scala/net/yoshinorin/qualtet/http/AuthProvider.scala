package net.yoshinorin.qualtet.http

import cats.effect.IO
import cats.Monad
import cats.data.{Kleisli, OptionT}
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRoutes, Request, Response, Status}
import org.http4s.headers.Authorization
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.auth.AuthService
import net.yoshinorin.qualtet.domains.errors.{AuthorNotFound, DomainError, Unauthorized}
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory => Log4CatsLoggerFactory, SelfAwareStructuredLogger}

class AuthProvider[F[_]: Monad](
  authService: AuthService[F]
)(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  private val logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  // TODO: cleanup messy code
  private def authUser: Kleisli[IO, Request[IO], Either[DomainError, (AuthorResponseModel, String)]] =
    Kleisli({ request =>
      val authorizationHeader = request.headers.get[Authorization].asEither[DomainError](Unauthorized("Authorization header is none")) match {
        case Right(auth: Authorization) => Right(auth)
        case Left(f: DomainError) => Left(f)
      }

      authorizationHeader match {
        case Left(e) => IO.pure(Left(e))
        case Right(a) => {
          (for {
            renderString <- IO(a.credentials.renderString.replace("Bearer ", "").replace("bearer ", ""))
            maybeAuthor <- authService.findAuthorFromJwtString(renderString)
            author <- IO(maybeAuthor.asEither[DomainError](AuthorNotFound(detail = "author not found")))
            payload <- request.as[String]
          } yield {
            author match {
              case Left(fail) => {
                logger.error(fail.getMessage) *>
                  IO.pure(Left(fail))
              }
              case Right(value) =>
                logger.info(s"Authorization succeeded: ${value}") *>
                  IO.pure(Right((value, payload)))
            }
          }).flatMap(identity)
        }
      }
    })

  private def onFailure: AuthedRoutes[DomainError, IO] = Kleisli { req =>
    OptionT.pure[IO](Response[IO](status = Status.Unauthorized))
  }

  def authenticate: AuthMiddleware[IO, (AuthorResponseModel, String)] = AuthMiddleware(authUser, onFailure)
}
