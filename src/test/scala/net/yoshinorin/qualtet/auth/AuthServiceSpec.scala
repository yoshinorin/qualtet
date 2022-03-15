package net.yoshinorin.qualtet.auth

import net.yoshinorin.qualtet.domains.models.Fail.{NotFound, Unauthorized}
import net.yoshinorin.qualtet.domains.models.authors.ResponseAuthor
import net.yoshinorin.qualtet.fixture.Fixture._
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.auth.AuthServiceSpec
class AuthServiceSpec extends AnyWordSpec {

  implicit val doobieContext: DoobieContext = new DoobieContext()

  val a: ResponseAuthor = authorService.findByName(author.name).unsafeRunSync().get

  "AuthService" should {

    "be generate token" in {
      val token = authService.generateToken(RequestToken(a.id, "pass")).unsafeRunSync().token
      assert(jwtInstance.decode(token).isRight)
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
