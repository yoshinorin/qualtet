package net.yoshinorin.qualtet.http

import cats.Monad
import cats.effect.Async
import cats.implicits.*
import cats.data.{Kleisli, OptionT}
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRoutes, Request, Response, Status}
import org.http4s.headers.Authorization
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.auth.AuthService
import net.yoshinorin.qualtet.domains.errors.{AuthorNotFound, DomainError, Unauthorized}
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class AuthProvider[F[_]: Async, G[_]: Monad @nowarn](
  authService: AuthService[F, G]
)(using loggerFactory: Log4CatsLoggerFactory[F]) {

  private val logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  // TODO: cleanup messy code
  private def authUser: Kleisli[F, Request[F], Either[DomainError, (AuthorResponseModel, String)]] =
    Kleisli({ request =>
      val authorizationHeader = request.headers.get[Authorization].asEither[DomainError](Unauthorized("Authorization header is none")) match {
        case Right(auth: Authorization) => Right(auth)
        case Left(f: DomainError) => Left(f)
      }

      authorizationHeader match {
        case Left(e) => Async[F].pure(Left(e))
        case Right(a) => {
          (for {
            renderString <- Async[F].delay(a.credentials.renderString.replace("Bearer ", "").replace("bearer ", ""))
            authorResult <- authService.findAuthorFromJwtString(renderString)
            author = authorResult.flatMap {
              case Some(a) => Right(a)
              case None => Left(AuthorNotFound(detail = "author not found"))
            }
            payload <- request.as[String]
          } yield {
            author match {
              case Left(fail) => {
                logger.error(fail.getMessage) *>
                  Async[F].pure(Left(fail))
              }
              case Right(value) =>
                logger.info(s"Authorization succeeded: ${value}") *>
                  Async[F].pure(Right((value, payload)))
            }
          }).flatMap(identity)
        }
      }
    })

  private def onFailure: AuthedRoutes[DomainError, F] = Kleisli { _ =>
    OptionT.pure[F](Response[F](status = Status.Unauthorized))
  }

  def authenticate: AuthMiddleware[F, (AuthorResponseModel, String)] = AuthMiddleware(authUser, onFailure)
}
