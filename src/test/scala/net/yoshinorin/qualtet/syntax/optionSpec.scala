package net.yoshinorin.qualtet.syntax

import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.message.Fail.UnprocessableEntity

// testOnly net.yoshinorin.qualtet.syntax.OptionSpec
class OptionSpec extends AnyWordSpec {

  "option syntax" should {

    "orThrow" should {

      "not be throw if not None" in {
        assert(Some("some values").orThrow(UnprocessableEntity(detail = "unprocessable!!")) === "some values")
      }

      "be thrown if None" in {
        assertThrows[UnprocessableEntity] {
          assert(None.orThrow(UnprocessableEntity(detail = "unprocessable!!")))
        }
      }

    }

    "asEither" should {

      "be return Right if not None" in {
        val result = Some("some values").asEither[Fail](UnprocessableEntity(detail = "unprocessable!!"))
        assert(result.isRight)
        assert(result.toOption.get === "some values")
      }

      "be return Left if None" in {
        val result = None.asEither[Fail](UnprocessableEntity(detail = "unprocessable!!"))
        assert(result.isLeft)
        assert(result.swap.getOrElse("").isInstanceOf[UnprocessableEntity])
      }

    }
  }

}
