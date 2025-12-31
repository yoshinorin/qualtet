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
        responseEither <- authService.generateToken(RequestToken(a.id, "pass"))
        response <- IO.fromEither(responseEither)
        decoded <- jwtInstance.decode(response.token)
      } yield {
        assert(decoded.isRight)
      }).unsafeRunSync()
    }

    "find an author from JWT string" in {
      (for {
        responseEither <- authService.generateToken(RequestToken(a.id, "pass"))
        response <- IO.fromEither(responseEither)
        authorEither <- authService.findAuthorFromJwtString(response.token)
        author <- IO.fromEither(authorEither)
      } yield {
        assert(author.get.id.value === a.id.value)
      }).unsafeRunSync()
    }

    "return Left(Unauthorized) when JWT is expired" in {
      val result = authService.findAuthorFromJwtString(expiredToken).unsafeRunSync()
      assert(result.isLeft)
      assert(result.left.exists(_.isInstanceOf[Unauthorized]))
    }

    "return Left(Unauthorized) when JWT's author not found" in {
      val result = authService.findAuthorFromJwtString(nonExistsUserToken).unsafeRunSync()
      assert(result.isLeft)
      assert(result.left.exists(_.isInstanceOf[Unauthorized]))
    }

    "return Left(Unauthorized) when password is wrong" in {
      val result = authService.generateToken(RequestToken(a.id, "wrongPassword")).unsafeRunSync()
      assert(result.isLeft)
      assert(result.left.exists(_.isInstanceOf[Unauthorized]))
    }

    "return Left(AuthorNotFound) when author not found" in {
      val result = authService.generateToken(RequestToken(authorId2, "password")).unsafeRunSync()
      assert(result.isLeft)
      assert(result.left.exists(_.isInstanceOf[AuthorNotFound]))
    }
  }

}
