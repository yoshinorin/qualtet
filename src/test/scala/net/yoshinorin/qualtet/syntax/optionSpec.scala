package net.yoshinorin.qualtet.syntax

import net.yoshinorin.qualtet.fixture.unsafe
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.errors.{DomainError, UnexpectedException}

// testOnly net.yoshinorin.qualtet.syntax.OptionSpec
class OptionSpec extends AnyWordSpec {

  "option syntax" should {

    "orThrow" should {

      "not be throw if not None" in {
        assert(Some("some values").orThrow(UnexpectedException(detail = "unprocessable!!")) === "some values")
      }

      "thrown if None" in {
        assertThrows[UnexpectedException] {
          assert(None.orThrow(UnexpectedException(detail = "unprocessable!!")))
        }
      }

    }

    "asEither" should {

      "return Right if not None" in {
        val result = Some("some values").asEither[DomainError](UnexpectedException(detail = "unprocessable!!"))
        assert(result.isRight)
        assert(result.unsafe === "some values")
      }

      "return Left if None" in {
        val result = None.asEither[DomainError](UnexpectedException(detail = "unprocessable!!"))
        assert(result.isLeft)
        assert(result.swap.getOrElse("").isInstanceOf[UnexpectedException])
      }

    }
  }

}
