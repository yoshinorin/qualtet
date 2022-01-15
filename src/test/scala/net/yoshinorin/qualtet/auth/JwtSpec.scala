package net.yoshinorin.qualtet.auth

import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.models.authors.{Author, AuthorDisplayName, AuthorId, AuthorName}
import net.yoshinorin.qualtet.fixture.Fixture.validBCryptPassword
import org.scalatest.wordspec.AnyWordSpec
import pdi.jwt.JwtAlgorithm
import pdi.jwt.exceptions.JwtValidationException

import java.security.SecureRandom
import wvlet.airframe.ulid.ULID

// testOnly net.yoshinorin.qualtet.auth.JwtSpec
class JwtSpec extends AnyWordSpec {

  "Jwt" should {
    val keyPair = new KeyPair("RSA", 2048, SecureRandom.getInstanceStrong)
    val message = SecureRandom.getInstanceStrong.toString.getBytes
    val signature = new net.yoshinorin.qualtet.auth.Signature("SHA256withRSA", message, keyPair)

    "encode and decode" in {
      val jwtInstance = new Jwt(JwtAlgorithm.RS256, keyPair, signature)
      val id = ULID.newULIDString.toLowerCase
      val jwtString = jwtInstance.encode(
        Author(
          id = AuthorId(id),
          name = AuthorName("Jhon"),
          displayName = AuthorDisplayName("JD"),
          password = validBCryptPassword
        )
      )

      jwtInstance.decode(jwtString) match {
        case Right(j) => {
          assert(j.iss == Config.jwtIss)
          assert(j.aud == Config.jwtAud)
          assert(j.sub == id)
        }
        case Left(l) =>
          println(l.getMessage)
          assert(false)
      }

    }

    "not signed JSON" in {
      val jwtInstance = new Jwt(JwtAlgorithm.RS256, keyPair, signature)
      val maybeJwtClaims = jwtInstance.decode(
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
      )
      assert(maybeJwtClaims.left.getOrElse("").isInstanceOf[JwtValidationException])
    }

    // TODO: check expired

  }

}
