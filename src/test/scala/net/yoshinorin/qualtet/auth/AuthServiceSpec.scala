package net.yoshinorin.qualtet.auth

import cats.effect.IO
import net.yoshinorin.qualtet.domains.services.AuthorService
import net.yoshinorin.qualtet.domains.models.Fail.{NotFound, Unauthorized}
import net.yoshinorin.qualtet.domains.models.authors.{Author, AuthorDisplayName, AuthorId, AuthorName, BCryptPassword}
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
      when(mockAuthorService.findByIdWithPassword(AuthorId("01FEBB8AZ5T42M2H68XJ8C754A")))
        .thenReturn(
          IO(
            Some(
              Author(
                id = AuthorId("01FEBB8AZ5T42M2H68XJ8C754A"),
                name = AuthorName("JhonDue"),
                displayName = AuthorDisplayName("JD"),
                password = BCryptPassword("$2a$10$XmRiVEV8yV9u8BnsIfSTTuzUvH/.6jutH6QvIX6zRoTcqkuKsxE0O")
              )
            )
          )
        )

      val authService = new AuthService(mockAuthorService, jwtInstance)
      val token = authService.generateToken(RequestToken(AuthorId("01FEBB8AZ5T42M2H68XJ8C754A"), "pass")).unsafeRunSync().token
      assert(jwtInstance.decode(token).isRight)
    }

    "password is wrong" in {
      when(mockAuthorService.findByIdWithPassword(AuthorId("01FEBB8AZ5T42M2H68XJ8C754A")))
        .thenReturn(
          IO(
            Some(
              Author(
                id = AuthorId("01FEBB8AZ5T42M2H68XJ8C754A"),
                name = AuthorName("JhonDue"),
                displayName = AuthorDisplayName("JD"),
                password = BCryptPassword("$2a$10$XmRiVEV8yV9u8BnsIfSTTuzUvH/.6jutH6QvIX6zRoTcqkuKsxE0O")
              )
            )
          )
        )

      val authService = new AuthService(mockAuthorService, jwtInstance)
      assertThrows[Unauthorized] {
        authService.generateToken(RequestToken(AuthorId("01FEBB8AZ5T42M2H68XJ8C754A"), "wrongPassword")).unsafeRunSync()
      }
    }

    "authorName not found" in {
      when(mockAuthorService.findByIdWithPassword(AuthorId("01FEBB8AZ5T42M2H68XJ8C754B")))
        .thenReturn(IO(None))

      val authService = new AuthService(mockAuthorService, jwtInstance)
      assertThrows[NotFound] {
        authService.generateToken(RequestToken(AuthorId("01FEBB8AZ5T42M2H68XJ8C754B"), "password")).unsafeRunSync()
      }
    }
  }

}
