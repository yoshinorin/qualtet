package net.yoshinorin.qualtet.validator

import cats.implicits.catsSyntaxEq
import net.yoshinorin.qualtet.error.Fail.{Unauthorized, UnprocessableEntity}
import net.yoshinorin.qualtet.syntax._
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.validator.ValidatorSpec
class ValidatorSpec extends AnyWordSpec {

  "validate" should {
    "be return right" in {
      // NOTE: Workaround avoid compile error when use `===`. So, use `eqv` instead of it.
      assert(Validator.validate("a")(x => x eqv "a")(UnprocessableEntity("unprocessable!!")).value.unsafeRunSync().isRight)
      assert(Validator.validate(1)(x => x eqv 1)(UnprocessableEntity("unprocessable!!")).value.unsafeRunSync().isRight)
    }

    "not be throw if isRight" in {
      val s = Validator.validate("a")(x => x eqv "a")(UnprocessableEntity("unprocessable!!")).andThrow.unsafeRunSync()
      assert(s eqv "a")
      val i = Validator.validate(1)(x => x eqv 1)(UnprocessableEntity("unprocessable!!")).andThrow.unsafeRunSync()
      assert(i eqv 1)
    }

    "be return left" in {
      assert(Validator.validate("a")(x => x =!= "a")(Unauthorized()).value.unsafeRunSync().isLeft)
      assert(Validator.validate(1)(x => x =!= 1)(UnprocessableEntity("unprocessable!!")).value.unsafeRunSync().isLeft)
    }

    "be throw if isLeft" in {
      assertThrows[Unauthorized] {
        Validator.validate("a")(x => x =!= "a")(Unauthorized()).andThrow.unsafeRunSync()
      }

      assertThrows[UnprocessableEntity] {
        Validator.validate(1)(x => x =!= 1)(UnprocessableEntity("unprocessable!!")).andThrow.unsafeRunSync()
      }
    }
  }

  "validateUnless" should {
    "be return right" in {
      assert(Validator.validateUnless("a")(x => x =!= "a")(UnprocessableEntity("unprocessable!!")).value.unsafeRunSync().isRight)
      assert(Validator.validateUnless(1)(x => x =!= 1)(UnprocessableEntity("unprocessable!!")).value.unsafeRunSync().isRight)
    }

    "not be throw if isRight" in {
      val s = Validator.validateUnless("a")(x => x =!= "a")(UnprocessableEntity("unprocessable!!")).andThrow.unsafeRunSync()
      assert(s eqv "a")
      val i = Validator.validateUnless(1)(x => x =!= 1)(UnprocessableEntity("unprocessable!!")).andThrow.unsafeRunSync()
      assert(i eqv 1)
    }

    "be return left" in {
      assert(Validator.validateUnless("a")(x => x eqv "a")(Unauthorized()).value.unsafeRunSync().isLeft)
      assert(Validator.validateUnless(1)(x => x eqv 1)(UnprocessableEntity("unprocessable!!")).value.unsafeRunSync().isLeft)
    }

    "be throw if isLeft" in {
      assertThrows[Unauthorized] {
        Validator.validateUnless("a")(x => x eqv "a")(Unauthorized()).andThrow.unsafeRunSync()
      }

      assertThrows[UnprocessableEntity] {
        Validator.validateUnless(1)(x => x eqv 1)(UnprocessableEntity("unprocessable!!")).andThrow.unsafeRunSync()
      }
    }
  }

}
