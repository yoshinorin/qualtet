package net.yoshinorin.qualtet.auth

import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.models.authors.{Author, AuthorDisplayName, AuthorId, AuthorName}
import net.yoshinorin.qualtet.fixture.Fixture.{jwtInstance, validBCryptPassword}
import org.scalatest.wordspec.AnyWordSpec
import pdi.jwt.exceptions.JwtValidationException
import wvlet.airframe.ulid.ULID

import java.time.Instant

// testOnly net.yoshinorin.qualtet.auth.JwtSpec
class JwtSpec extends AnyWordSpec {

  val jc: JwtClaim = JwtClaim("http://localhost:9001", "qualtet_dev_1111", "01fy9g4m9wsg7frfct8m5rtxz0", "01fy9g4n10sf2vxmhmxq7h9x42", 1647442477, 1647438877)

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

      jwtInstance.decode(jwtString).unsafeRunSync() match {
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
      assert(maybeJwtClaims.unsafeRunSync().left.getOrElse("").isInstanceOf[JwtValidationException])
    }

    "be return Right if JWT is correct" in {
      assert(jwtInstance.claimValidator(jc)(x => x.aud == "qualtet_dev_1111")("").value.unsafeRunSync().isRight)
      assert(jwtInstance.claimValidator(jc)(x => x.iss == "http://localhost:9001")("").value.unsafeRunSync().isRight)
      assert(jwtInstance.claimValidator(jc)(x => x.exp > Instant.now.getEpochSecond)("").value.unsafeRunSync().isRight)
    }

    "be return Left if JWT is incorrect" in {
      assert(jwtInstance.claimValidator(jc)(x => x.aud == "incorrect aud")("").value.unsafeRunSync().isLeft)
      assert(jwtInstance.claimValidator(jc)(x => x.iss == "incorrect iss")("").value.unsafeRunSync().isLeft)
      assert(jwtInstance.claimValidator(jc)(x => x.exp > x.exp + 1)("").value.unsafeRunSync().isLeft)
    }

    "be return Left if JWT is incorrect in for-comprehension: pattern-one" in {
      val r = for {
        _ <- jwtInstance.claimValidator(jc)(x => x.aud == Config.jwtAud)("")
        _ <- jwtInstance.claimValidator(jc)(x => x.iss == Config.jwtIss)("")
        result <- jwtInstance.claimValidator(jc)(x => x.exp > x.exp + 1)("")
      } yield result

      assert(r.value.unsafeRunSync().isLeft)
    }

    "be return Left if JWT is incorrect in for-comprehension: pattern-two" in {
      val r = for {
        _ <- jwtInstance.claimValidator(jc)(x => x.aud == "qualtet_dev_1111")("")
        _ <- jwtInstance.claimValidator(jc)(x => x.iss == "incorrect iss")("")
        result <- jwtInstance.claimValidator(jc)(x => x.exp > Instant.now.getEpochSecond)("")
      } yield result

      assert(r.value.unsafeRunSync().isLeft)
    }

    "be return Left if JWT is incorrect in for-comprehension: pattern-three" in {
      val r = for {
        _ <- jwtInstance.claimValidator(jc)(x => x.aud == "incorrect aud")("")
        _ <- jwtInstance.claimValidator(jc)(x => x.iss == "http://localhost:9001")("")
        result <- jwtInstance.claimValidator(jc)(x => x.exp > Instant.now.getEpochSecond)("")
      } yield result

      assert(r.value.unsafeRunSync().isLeft)
    }

  }

}
