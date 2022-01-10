package net.yoshinorin.qualtet.auth

import cats.effect.IO
import net.yoshinorin.qualtet.domains.services.AuthorService
import net.yoshinorin.qualtet.domains.models.Fail.{NotFound, Unauthorized}
import net.yoshinorin.qualtet.fixture.Fixture._
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.wordspec.AnyWordSpec
import pdi.jwt.JwtAlgorithm

import java.security.SecureRandom

// testOnly net.yoshinorin.qualtet.auth.AuthServiceSpec
class AuthServiceSpec extends AnyWordSpec {

  implicit val doobieContext: DoobieContext = new DoobieContext()

  val keyPair = new KeyPair("RSA", 2048, SecureRandom.getInstanceStrong)
  val message: Array[Byte] = SecureRandom.getInstanceStrong.toString.getBytes
  val signature = new net.yoshinorin.qualtet.auth.Signature("SHA256withRSA", message, keyPair)
  val jwtInstance = new Jwt(JwtAlgorithm.RS256, keyPair, signature)

  val mockAuthorService: AuthorService = Mockito.mock(classOf[AuthorService])

  "AuthService" should {

    "generate token" in {
      when(mockAuthorService.findByIdWithPassword(authorId))
        .thenReturn(IO(Some(author)))

      val authService = new AuthService(mockAuthorService, jwtInstance)
      val token = authService.generateToken(RequestToken(authorId, "pass")).unsafeRunSync().token
      assert(jwtInstance.decode(token).isRight)
    }

    "password is wrong" in {
      when(mockAuthorService.findByIdWithPassword(authorId))
        .thenReturn(IO(Some(author)))

      val authService = new AuthService(mockAuthorService, jwtInstance)
      assertThrows[Unauthorized] {
        authService.generateToken(RequestToken(authorId, "wrongPassword")).unsafeRunSync()
      }
    }

    "authorName not found" in {
      when(mockAuthorService.findByIdWithPassword(authorId2))
        .thenReturn(IO(None))

      val authService = new AuthService(mockAuthorService, jwtInstance)
      assertThrows[NotFound] {
        authService.generateToken(RequestToken(authorId2, "password")).unsafeRunSync()
      }
    }
  }

}
