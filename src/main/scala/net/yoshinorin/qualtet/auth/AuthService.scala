package net.yoshinorin.qualtet.auth

import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.authors.{AuthorId, AuthorResponseModel, AuthorService, BCryptPassword}
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import net.yoshinorin.qualtet.domains.errors.{AuthorNotFound, Unauthorized}
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

class AuthService[F[_]: Monad](authorService: AuthorService[F], jwt: Jwt[IO])(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  private val logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)
  private val bcryptPasswordEncoder = new BCryptPasswordEncoder()

  def generateToken(tokenRequest: RequestToken): IO[ResponseToken] = {

    def verifyPassword(password: BCryptPassword): IO[Unit] = {
      if (bcryptPasswordEncoder.matches(tokenRequest.password, password.value)) {
        IO(())
      } else {
        logger.error(s"authorId: ${tokenRequest.authorId} - wrong password") *>
          IO.raiseError(Unauthorized())
      }
    }

    for {
      a <- authorService
        .findByIdWithPassword(tokenRequest.authorId)
        .errorIfNone(AuthorNotFound(detail = s"${tokenRequest.authorId} is not found."))
        .flatMap(_.liftTo[IO])
      _ <- verifyPassword(a.password)
      jwt <- jwt.encode(a)
      responseToken <- IO(ResponseToken(jwt))
    } yield responseToken

  }

  def findAuthorFromJwtString(jwtString: String): IO[Option[AuthorResponseModel]] = {
    jwt.decode(jwtString).flatMap {
      case Right(jwtClaim: JwtClaim) =>
        authorService.findById(AuthorId(jwtClaim.sub))
      case Left(t: Throwable) =>
        logger.error(s"${t.getMessage}") *>
          IO.pure(throw Unauthorized())
    }
  }

}
