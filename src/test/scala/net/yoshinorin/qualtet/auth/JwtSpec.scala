package net.yoshinorin.qualtet.auth

import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.models.Fail.Unauthorized
import net.yoshinorin.qualtet.domains.models.authors.{Author, AuthorDisplayName, AuthorId, AuthorName}
import net.yoshinorin.qualtet.fixture.Fixture.{expiredToken, jwtInstance, validBCryptPassword}
import org.scalatest.wordspec.AnyWordSpec
import pdi.jwt.exceptions.JwtValidationException

import wvlet.airframe.ulid.ULID

// testOnly net.yoshinorin.qualtet.auth.JwtSpec
class JwtSpec extends AnyWordSpec {

  "Jwt" should {
    "be encode and decode" in {
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

    "be throw exception caused by not signed JSON" in {
      val maybeJwtClaims = jwtInstance.decode(
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
      )
      assert(maybeJwtClaims.left.getOrElse("").isInstanceOf[JwtValidationException])
    }

    // TODO: check expired

  }

}
