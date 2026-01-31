package net.yoshinorin.qualtet.auth

import cats.data.EitherT
import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.domains.authors.{AuthorId, AuthorResponseModel, AuthorService, BCryptPassword}
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import net.yoshinorin.qualtet.domains.errors.{AuthorNotFound, DomainError, Unauthorized}
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class AuthService[F[_]: Monad @nowarn](authorService: AuthorService[F], jwt: Jwt[IO])(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  private val logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)
  private val bcryptPasswordEncoder = new BCryptPasswordEncoder()

  def generateToken(tokenRequest: RequestToken): IO[Either[DomainError, ResponseToken]] = {

    def verifyPassword(password: BCryptPassword): IO[Either[Unauthorized, Unit]] = {
      if (bcryptPasswordEncoder.matches(tokenRequest.password, password.value)) {
        IO.pure(Right(()))
      } else {
        logger.error(s"authorId: ${tokenRequest.authorId} - wrong password") *>
          IO.pure(Left(Unauthorized()))
      }
    }

    (for {
      a <- EitherT(
        authorService
          .findByIdWithPassword(tokenRequest.authorId)
          .errorIfNone(AuthorNotFound(detail = s"${tokenRequest.authorId} is not found."))
      )
      _ <- EitherT(verifyPassword(a.password))
      jwt <- EitherT.liftF(jwt.encode(a))
    } yield ResponseToken(jwt)).value

  }

  def findAuthorFromJwtString(jwtString: String): IO[Either[DomainError, Option[AuthorResponseModel]]] = {
    jwt.decode(jwtString).flatMap {
      case Right(jwtClaim: JwtClaim) =>
        authorService.findById(AuthorId(jwtClaim.sub)).map(Right(_))
      case Left(t: Throwable) =>
        logger.error(s"${t.getMessage}") *>
          IO.pure(Left(Unauthorized()))
    }
  }

}
