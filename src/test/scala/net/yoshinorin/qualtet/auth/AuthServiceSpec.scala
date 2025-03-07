package net.yoshinorin.qualtet.auth

import cats.effect.IO
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.domains.errors.{AuthorNotFound, Unauthorized}
import net.yoshinorin.qualtet.Modules
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.auth.AuthServiceSpec
class AuthServiceSpec extends AnyWordSpec {

  val mod = Modules(fixtureTx)
  val a: AuthorResponseModel = authorService.findByName(author.name).unsafeRunSync().get
  val a2: AuthorResponseModel = authorService.findByName(author2.name).unsafeRunSync().get

  "AuthService" should {

    "generate token" in {
      val token = mod.authService.generateToken(RequestToken(a.id, "pass")).unsafeRunSync().token
      assert(mod.jwtInstance.decode[IO](token).unsafeRunSync().isRight)
    }

    "find an author from JWT string" in {
      val token = authService.generateToken(RequestToken(a.id, "pass")).unsafeRunSync().token
      val author = authService.findAuthorFromJwtString(token).unsafeRunSync().get
      assert(author.id.value === a.id.value)
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
