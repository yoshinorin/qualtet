package net.yoshinorin.qualtet.utils

import net.yoshinorin.qualtet.domains.models.Fail.{Unauthorized, UnprocessableEntity}
import net.yoshinorin.qualtet.validator.Validator
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.utils.ValidatorSpec
class ValidatorSpec extends AnyWordSpec {

  "validate" should {
    "be return right" in {
      assert(Validator.validate("a")(x => x == "a")(UnprocessableEntity("unprocessable!!")).value.unsafeRunSync().isRight)
      assert(Validator.validate(1)(x => x == 1)(UnprocessableEntity("unprocessable!!")).value.unsafeRunSync().isRight)
    }

    "be return left" in {
      assert(Validator.validate("a")(x => x != "a")(Unauthorized()).value.unsafeRunSync().isLeft)
      assert(Validator.validate(1)(x => x != 1)(UnprocessableEntity("unprocessable!!")).value.unsafeRunSync().isLeft)
    }
  }

}
