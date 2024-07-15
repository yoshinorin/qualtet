package net.yoshinorin.qualtet.syntax

import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.errors.{Error, UnprocessableEntity}

// testOnly net.yoshinorin.qualtet.syntax.OptionSpec
class OptionSpec extends AnyWordSpec {

  "option syntax" should {

    "orThrow" should {

      "not be throw if not None" in {
        assert(Some("some values").orThrow(UnprocessableEntity(detail = "unprocessable!!")) === "some values")
      }

      "thrown if None" in {
        assertThrows[UnprocessableEntity] {
          assert(None.orThrow(UnprocessableEntity(detail = "unprocessable!!")))
        }
      }

    }

    "asEither" should {

      "return Right if not None" in {
        val result = Some("some values").asEither[Error](UnprocessableEntity(detail = "unprocessable!!"))
        assert(result.isRight)
        assert(result.toOption.get === "some values")
      }

      "return Left if None" in {
        val result = None.asEither[Error](UnprocessableEntity(detail = "unprocessable!!"))
        assert(result.isLeft)
        assert(result.swap.getOrElse("").isInstanceOf[UnprocessableEntity])
      }

    }
  }

}
