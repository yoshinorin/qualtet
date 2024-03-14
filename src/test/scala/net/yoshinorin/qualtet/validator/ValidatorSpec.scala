package net.yoshinorin.qualtet.validator

import cats.implicits.catsSyntaxEq
import cats.effect.IO
import net.yoshinorin.qualtet.message.Fail.{Unauthorized, UnprocessableEntity}
import net.yoshinorin.qualtet.syntax.*
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.validator.ValidatorSpec
class ValidatorSpec extends AnyWordSpec {

  val ioInstance = implicitly[cats.Monad[IO]]

  "validate" should {
    "return right" in {
      // NOTE: Workaround avoid compile error when use `===`. So, use `eqv` instead of it.
      assert(Validator.validate("a")(x => x eqv "a")(UnprocessableEntity(detail = "unprocessable!!"))(using ioInstance).value.unsafeRunSync().isRight)
      assert(Validator.validate(1)(x => x eqv 1)(UnprocessableEntity(detail = "unprocessable!!"))(using ioInstance).value.unsafeRunSync().isRight)
    }

    "not be throw if isRight" in {
      val s = Validator.validate("a")(x => x eqv "a")(UnprocessableEntity(detail = "unprocessable!!"))(using ioInstance).andThrow.unsafeRunSync()
      assert(s eqv "a")
      val i = Validator.validate(1)(x => x eqv 1)(UnprocessableEntity(detail = "unprocessable!!"))(using ioInstance).andThrow.unsafeRunSync()
      assert(i eqv 1)
    }

    "return left" in {
      assert(Validator.validate("a")(x => x =!= "a")(Unauthorized())(using ioInstance).value.unsafeRunSync().isLeft)
      assert(Validator.validate(1)(x => x =!= 1)(UnprocessableEntity(detail = "unprocessable!!"))(using ioInstance).value.unsafeRunSync().isLeft)
    }

    "throw if isLeft" in {
      assertThrows[Unauthorized] {
        Validator.validate("a")(x => x =!= "a")(Unauthorized())(using ioInstance).andThrow.unsafeRunSync()
      }

      assertThrows[UnprocessableEntity] {
        Validator.validate(1)(x => x =!= 1)(UnprocessableEntity(detail = "unprocessable!!"))(using ioInstance).andThrow.unsafeRunSync()
      }
    }
  }

  "validateUnless" should {
    "return right" in {
      assert(Validator.validateUnless("a")(x => x =!= "a")(UnprocessableEntity(detail = "unprocessable!!"))(using ioInstance).value.unsafeRunSync().isRight)
      assert(Validator.validateUnless(1)(x => x =!= 1)(UnprocessableEntity(detail = "unprocessable!!"))(using ioInstance).value.unsafeRunSync().isRight)
    }

    "not be throw if isRight" in {
      val s = Validator.validateUnless("a")(x => x =!= "a")(UnprocessableEntity(detail = "unprocessable!!"))(using ioInstance).andThrow.unsafeRunSync()
      assert(s eqv "a")
      val i = Validator.validateUnless(1)(x => x =!= 1)(UnprocessableEntity(detail = "unprocessable!!"))(using ioInstance).andThrow.unsafeRunSync()
      assert(i eqv 1)
    }

    "return left" in {
      assert(Validator.validateUnless("a")(x => x eqv "a")(Unauthorized())(using ioInstance).value.unsafeRunSync().isLeft)
      assert(Validator.validateUnless(1)(x => x eqv 1)(UnprocessableEntity(detail = "unprocessable!!"))(using ioInstance).value.unsafeRunSync().isLeft)
    }

    "throw if isLeft" in {
      assertThrows[Unauthorized] {
        Validator.validateUnless("a")(x => x eqv "a")(Unauthorized())(using ioInstance).andThrow.unsafeRunSync()
      }

      assertThrows[UnprocessableEntity] {
        Validator.validateUnless(1)(x => x eqv 1)(UnprocessableEntity(detail = "unprocessable!!"))(using ioInstance).andThrow.unsafeRunSync()
      }
    }
  }

}
