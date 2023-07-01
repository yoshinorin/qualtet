package net.yoshinorin.qualtet.auth

import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.message.Fail.{NotFound, Unauthorized}
import net.yoshinorin.qualtet.Modules.*
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieTransactor
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.auth.AuthServiceSpec
class AuthServiceSpec extends AnyWordSpec {

  given dbContext: DoobieTransactor = new DoobieTransactor(config.db)

  val a: ResponseAuthor = authorService.findByName(author.name).unsafeRunSync().get
  val a2: ResponseAuthor = authorService.findByName(author2.name).unsafeRunSync().get

  "AuthService" should {

    "be generate token" in {
      val token = authService.generateToken(RequestToken(a.id, "pass")).unsafeRunSync().token
      assert(jwtInstance.decode(token).unsafeRunSync().isRight)
    }

    "be find an author from JWT string" in {
      val token = authService.generateToken(RequestToken(a.id, "pass")).unsafeRunSync().token
      val author = authService.findAuthorFromJwtString(token).unsafeRunSync().get
      assert(author.id.value === a.id.value)
    }

    "be throw exception if JWT is expired" in {
      assertThrows[Unauthorized] {
        authService.findAuthorFromJwtString(expiredToken).unsafeRunSync()
      }
    }

    "be throw exception if JWT's author not found" in {
      assertThrows[Unauthorized] {
        authService.findAuthorFromJwtString(nonExistsUserToken).unsafeRunSync()
      }
    }

    "be throw exception caused by password is wrong" in {
      assertThrows[Unauthorized] {
        authService.generateToken(RequestToken(a.id, "wrongPassword")).unsafeRunSync()
      }
    }

    "be throw exception caused by authorName not found" in {
      assertThrows[NotFound] {
        authService.generateToken(RequestToken(authorId2, "password")).unsafeRunSync()
      }
    }
  }

}
