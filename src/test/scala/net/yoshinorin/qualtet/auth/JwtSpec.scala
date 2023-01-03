package net.yoshinorin.qualtet.auth

import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.authors.{Author, AuthorDisplayName, AuthorId, AuthorName}
import net.yoshinorin.qualtet.message.Fail.Unauthorized
import net.yoshinorin.qualtet.Modules._
import net.yoshinorin.qualtet.fixture.Fixture.{validBCryptPassword}
import net.yoshinorin.qualtet.validator.Validator
import org.scalatest.wordspec.AnyWordSpec
import pdi.jwt.exceptions.JwtValidationException
import wvlet.airframe.ulid.ULID
import net.yoshinorin.qualtet.syntax._

import cats.effect.unsafe.implicits.global

import java.time.Instant

// testOnly net.yoshinorin.qualtet.auth.JwtSpec
class JwtSpec extends AnyWordSpec {

  val jc: JwtClaim = JwtClaim(
    iss = Config.jwtIss,
    aud = Config.jwtAud,
    sub = "01fy9g4m9wsg7frfct8m5rtxz0",
    jti = "01fy9g4n10sf2vxmhmxq7h9x42",
    exp = Instant.now.getEpochSecond,
    iat = Instant.now.getEpochSecond
  )

  "Jwt" should {
    "be encode and decode" in {
      val id = ULID.newULIDString.toLower
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
          assert(j.iss === Config.jwtIss)
          assert(j.aud === Config.jwtAud)
          assert(j.sub === id)

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
      assert(Validator.validate(jc)(x => x.aud === Config.jwtAud)(Unauthorized()).value.unsafeRunSync().isRight)
      assert(Validator.validate(jc)(x => x.iss === Config.jwtIss)(Unauthorized()).value.unsafeRunSync().isRight)
      assert(Validator.validate(jc)(x => x.exp > Instant.now.getEpochSecond - 1000)(Unauthorized()).value.unsafeRunSync().isRight)
    }

    "be return Left if JWT is incorrect" in {
      assert(Validator.validate(jc)(x => x.aud === "incorrect aud")(Unauthorized()).value.unsafeRunSync().isLeft)
      assert(Validator.validate(jc)(x => x.iss === "incorrect iss")(Unauthorized()).value.unsafeRunSync().isLeft)
      assert(Validator.validate(jc)(x => x.exp > x.exp + 1)(Unauthorized()).value.unsafeRunSync().isLeft)
    }

    "be return Left if JWT is incorrect in for-comprehension: pattern-one" in {
      val r = for {
        _ <- Validator.validate(jc)(x => x.aud === Config.jwtAud)(Unauthorized())
        _ <- Validator.validate(jc)(x => x.iss === Config.jwtIss)(Unauthorized())
        result <- Validator.validate(jc)(x => x.exp > (x.exp + Config.jwtExpiration + 1))(Unauthorized())
      } yield result

      assert(r.value.unsafeRunSync().isLeft)
    }

    "be return Left if JWT is incorrect in for-comprehension: pattern-two" in {
      val r = for {
        _ <- Validator.validate(jc)(x => x.aud === Config.jwtAud)(Unauthorized())
        _ <- Validator.validate(jc)(x => x.iss === "incorrect iss")(Unauthorized())
        result <- Validator.validate(jc)(x => x.exp > Instant.now.getEpochSecond)(Unauthorized())
      } yield result

      assert(r.value.unsafeRunSync().isLeft)
    }

    "be return Left if JWT is incorrect in for-comprehension: pattern-three" in {
      val r = for {
        _ <- Validator.validate(jc)(x => x.aud === "incorrect aud")(Unauthorized())
        _ <- Validator.validate(jc)(x => x.iss === Config.jwtIss)(Unauthorized())
        result <- Validator.validate(jc)(x => x.exp > Instant.now.getEpochSecond)(Unauthorized())
      } yield result

      assert(r.value.unsafeRunSync().isLeft)
    }

  }

}
