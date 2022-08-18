package net.yoshinorin.qualtet.syntax

import cats.implicits.catsSyntaxEq
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.message.Fail.{Unauthorized, UnprocessableEntity}
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.syntax.ValidatorSpec
class ValidatorSpec extends AnyWordSpec {

  "validate" should {
    "be return right" in {
      // NOTE: Workaround avoid compile error when use `===`. So, use `eqv` instead of it.
      assert("a".toEitherIO(x => x eqv "a")(UnprocessableEntity("unprocessable!!")).value.unsafeRunSync().isRight)
      assert(1.toEitherIO(x => x eqv 1)(UnprocessableEntity("unprocessable!!")).value.unsafeRunSync().isRight)
    }

    "not be throw if isRight" in {
      val s = "a".toEitherIO(x => x eqv "a")(UnprocessableEntity("unprocessable!!")).andThrow.unsafeRunSync()
      assert(s eqv "a")
      val i = 1.toEitherIO(x => x eqv 1)(UnprocessableEntity("unprocessable!!")).andThrow.unsafeRunSync()
      assert(i eqv 1)
    }

    "be return left" in {
      assert("a".toEitherIO(x => x =!= "a")(Unauthorized()).value.unsafeRunSync().isLeft)
      assert(1.toEitherIO(x => x =!= 1)(UnprocessableEntity("unprocessable!!")).value.unsafeRunSync().isLeft)
    }

    "be throw if isLeft" in {
      assertThrows[Unauthorized] {
        "a".toEitherIO(x => x =!= "a")(Unauthorized()).andThrow.unsafeRunSync()
      }

      assertThrows[UnprocessableEntity] {
        1.toEitherIO(x => x =!= 1)(UnprocessableEntity("unprocessable!!")).andThrow.unsafeRunSync()
      }
    }
  }

}
