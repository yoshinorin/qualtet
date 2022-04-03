package net.yoshinorin.qualtet.validator

import net.yoshinorin.qualtet.domains.models.Fail.{Unauthorized, UnprocessableEntity}
import net.yoshinorin.qualtet.validator.Compositions._
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.validator.ValidatorSpec
class ValidatorSpec extends AnyWordSpec {

  "validate" should {
    "be return right" in {
      assert(Validator.validate("a")(x => x == "a")(UnprocessableEntity("unprocessable!!")).value.unsafeRunSync().isRight)
      assert(Validator.validate(1)(x => x == 1)(UnprocessableEntity("unprocessable!!")).value.unsafeRunSync().isRight)
    }

    "not be throw if isRight" in {
      val s = Validator.validate("a")(x => x == "a")(UnprocessableEntity("unprocessable!!")).andThrow.unsafeRunSync()
      assert(s == "a")
      val i = Validator.validate(1)(x => x == 1)(UnprocessableEntity("unprocessable!!")).andThrow.unsafeRunSync()
      assert(i == 1)
    }

    "be return left" in {
      assert(Validator.validate("a")(x => x != "a")(Unauthorized()).value.unsafeRunSync().isLeft)
      assert(Validator.validate(1)(x => x != 1)(UnprocessableEntity("unprocessable!!")).value.unsafeRunSync().isLeft)
    }

    "be throw if isLeft" in {
      assertThrows[Unauthorized] {
        Validator.validate("a")(x => x != "a")(Unauthorized()).andThrow.unsafeRunSync()
      }

      assertThrows[UnprocessableEntity] {
        Validator.validate(1)(x => x != 1)(UnprocessableEntity("unprocessable!!")).andThrow.unsafeRunSync()
      }
    }
  }

}
