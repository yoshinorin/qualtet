package net.yoshinorin.qualtet.auth

import cats.effect.IO
import net.yoshinorin.qualtet.domains.authors.{Author, AuthorDisplayName, AuthorId, AuthorName}
import net.yoshinorin.qualtet.message.Fail.Unauthorized
import net.yoshinorin.qualtet.Modules.*
import net.yoshinorin.qualtet.fixture.Fixture.validBCryptPassword
import net.yoshinorin.qualtet.validator.Validator
import org.scalatest.wordspec.AnyWordSpec
import pdi.jwt.exceptions.JwtValidationException
import wvlet.airframe.ulid.ULID
import net.yoshinorin.qualtet.syntax.*

import cats.effect.unsafe.implicits.global

import java.time.Instant

// testOnly net.yoshinorin.qualtet.auth.JwtSpec
class JwtSpec extends AnyWordSpec {

  val jc: JwtClaim = JwtClaim(
    iss = config.jwt.iss,
    aud = config.jwt.aud,
    sub = "01fy9g4m9wsg7frfct8m5rtxz0",
    jti = "01fy9g4n10sf2vxmhmxq7h9x42",
    exp = Instant.now.getEpochSecond,
    iat = Instant.now.getEpochSecond
  )

  "Jwt" should {
    "encode and decode" in {
      val id = ULID.newULIDString.toLower
      val jwtString = jwtInstance.encode(
        Author(
          id = AuthorId(id),
          name = AuthorName("Jhon"),
          displayName = AuthorDisplayName("JD"),
          password = validBCryptPassword
        )
      )

      jwtInstance.decode[IO](jwtString).unsafeRunSync() match {
        case Right(j) => {
          assert(j.iss === config.jwt.iss)
          assert(j.aud === config.jwt.aud)
          assert(j.sub === id)

        }
        case Left(l) =>
          assert(false)
      }

    }

    val ioInstance = implicitly[cats.Monad[IO]]

    "throw exception caused by not signed JSON" in {
      val maybeJwtClaims = jwtInstance.decode[IO](
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
      )
      assert(maybeJwtClaims.unsafeRunSync().left.getOrElse("").isInstanceOf[JwtValidationException])
    }

    "return Right if JWT is correct" in {
      assert(Validator.validate(jc)(x => x.aud === config.jwt.aud)(Unauthorized())(using ioInstance).value.unsafeRunSync().isRight)
      assert(Validator.validate(jc)(x => x.iss === config.jwt.iss)(Unauthorized())(using ioInstance).value.unsafeRunSync().isRight)
      assert(Validator.validate(jc)(x => x.exp > Instant.now.getEpochSecond - 1000)(Unauthorized())(using ioInstance).value.unsafeRunSync().isRight)
    }

    "return Left if JWT is incorrect" in {
      assert(Validator.validate(jc)(x => x.aud === "incorrect aud")(Unauthorized())(using ioInstance).value.unsafeRunSync().isLeft)
      assert(Validator.validate(jc)(x => x.iss === "incorrect iss")(Unauthorized())(using ioInstance).value.unsafeRunSync().isLeft)
      assert(Validator.validate(jc)(x => x.exp > x.exp + 1)(Unauthorized())(using ioInstance).value.unsafeRunSync().isLeft)
    }

    "return Left if JWT is incorrect in for-comprehension: pattern-one" in {
      val r = for {
        _ <- Validator.validate(jc)(x => x.aud === config.jwt.aud)(Unauthorized())(using ioInstance)
        _ <- Validator.validate(jc)(x => x.iss === config.jwt.iss)(Unauthorized())(using ioInstance)
        result <- Validator.validate(jc)(x => x.exp > (x.exp + config.jwt.expiration + 1))(Unauthorized())(using ioInstance)
      } yield result

      assert(r.value.unsafeRunSync().isLeft)
    }

    "return Left if JWT is incorrect in for-comprehension: pattern-two" in {
      val r = for {
        _ <- Validator.validate(jc)(x => x.aud === config.jwt.aud)(Unauthorized())(using ioInstance)
        _ <- Validator.validate(jc)(x => x.iss === "incorrect iss")(Unauthorized())(using ioInstance)
        result <- Validator.validate(jc)(x => x.exp > Instant.now.getEpochSecond)(Unauthorized())(using ioInstance)
      } yield result

      assert(r.value.unsafeRunSync().isLeft)
    }

    "return Left if JWT is incorrect in for-comprehension: pattern-three" in {
      val r = for {
        _ <- Validator.validate(jc)(x => x.aud === "incorrect aud")(Unauthorized())(using ioInstance)
        _ <- Validator.validate(jc)(x => x.iss === config.jwt.iss)(Unauthorized())(using ioInstance)
        result <- Validator.validate(jc)(x => x.exp > Instant.now.getEpochSecond)(Unauthorized())(using ioInstance)
      } yield result

      assert(r.value.unsafeRunSync().isLeft)
    }

  }

}
