package net.yoshinorin.qualtet.auth

import cats.effect.IO
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.domains.errors.{AuthorNotFound, Unauthorized}
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.auth.AuthServiceSpec
class AuthServiceSpec extends AnyWordSpec {

  val a: AuthorResponseModel = authorService.findByName(author.name).unsafeRunSync().get

  "AuthService" should {

    "generate token" in {
      (for {
        response <- authService.generateToken(RequestToken(a.id, "pass"))
        decoded <- jwtInstance.decode[IO](response.token)
      } yield {
        assert(decoded.isRight)
      }).unsafeRunSync()
    }

    "find an author from JWT string" in {
      (for {
        response <- authService.generateToken(RequestToken(a.id, "pass"))
        author <- authService.findAuthorFromJwtString(response.token)
      } yield {
        assert(author.get.id.value === a.id.value)
      }).unsafeRunSync()
    }

    "throw exception if JWT is expired" in {
      assertThrows[Unauthorized] {
        authService.findAuthorFromJwtString(expiredToken).unsafeRunSync()
      }
    }

    "throw exception if JWT's author not found" in {
      assertThrows[Unauthorized] {
        authService.findAuthorFromJwtString(nonExistsUserToken).unsafeRunSync()
      }
    }

    "throw exception caused by password is wrong" in {
      assertThrows[Unauthorized] {
        authService.generateToken(RequestToken(a.id, "wrongPassword")).unsafeRunSync()
      }
    }

    "throw exception caused by authorName not found" in {
      assertThrows[AuthorNotFound] {
        authService.generateToken(RequestToken(authorId2, "password")).unsafeRunSync()
      }
    }
  }

}
