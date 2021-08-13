package net.yoshinorin.qualtet.auth

import cats.effect.IO
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import net.yoshinorin.qualtet.domains.models.Fail.{NotFound, Unauthorized}
import net.yoshinorin.qualtet.domains.models.authors.{Author, AuthorId, BCryptPassword}
import net.yoshinorin.qualtet.domains.services.AuthorService
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
        IO()
      } else {
        logger.error(s"authorId: ${tokenRequest.authorId} - wrong password")
        IO.raiseError(Unauthorized("unauthorized"))
      }
    }

    for {
      a <- author
      _ <- verifyPassword(a.password)
      jwt <- IO(jwt.encode(a))
      jwtString <- IO(ResponseToken(jwt))
    } yield jwtString

  }

}
