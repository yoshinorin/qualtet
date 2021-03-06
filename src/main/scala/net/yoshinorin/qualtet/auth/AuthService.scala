package net.yoshinorin.qualtet.auth

import cats.effect.IO
import net.yoshinorin.qualtet.domains.authors.{Author, AuthorId, AuthorService, BCryptPassword, ResponseAuthor}
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import net.yoshinorin.qualtet.message.Fail.{NotFound, Unauthorized}
import org.slf4j.LoggerFactory

class AuthService(authorService: AuthorService, jwt: Jwt) {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)
  private[this] val bcryptPasswordEncoder = new BCryptPasswordEncoder()

  def generateToken(tokenRequest: RequestToken): IO[ResponseToken] = {

    def author: IO[Author] = authorService.findByIdWithPassword(tokenRequest.authorId).flatMap {
      case None => IO.raiseError(NotFound(s"${tokenRequest.authorId} is not found."))
      case Some(x) => IO(x)
    }

    def verifyPassword(password: BCryptPassword): IO[Unit] = {
      if (bcryptPasswordEncoder.matches(tokenRequest.password, password.value)) {
        IO(())
      } else {
        logger.error(s"authorId: ${tokenRequest.authorId} - wrong password")
        IO.raiseError(Unauthorized("unauthorized"))
      }
    }

    for {
      a <- author
      _ <- verifyPassword(a.password)
      jwt <- IO(jwt.encode(a))
      responseToken <- IO(ResponseToken(jwt))
    } yield responseToken

  }

  def findAuthorFromJwtString(jwtString: String): IO[Option[ResponseAuthor]] = {
    jwt.decode(jwtString).flatMap {
      case Right(jwtClaim: JwtClaim) =>
        authorService.findById(AuthorId(jwtClaim.sub))
      case Left(t) =>
        logger.error(s"${t.getMessage}")
        throw Unauthorized()
    }
  }

}
