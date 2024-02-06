package net.yoshinorin.qualtet.syntax

import cats.implicits.catsSyntaxEq
import cats.effect.IO
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.message.Fail.{Unauthorized, UnprocessableEntity}
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.syntax.ValidatorSpec
class ValidatorSpec extends AnyWordSpec {

  val ioInstance = implicitly[cats.Monad[IO]]

  "validate" should {
    "be return right" in {

      // NOTE: Workaround avoid compile error when use `===`. So, use `eqv` instead of it.
      assert("a".toEitherF(x => x eqv "a")(UnprocessableEntity(detail = "unprocessable!!"))(ioInstance).value.unsafeRunSync().isRight)
      assert(1.toEitherF(x => x eqv 1)(UnprocessableEntity(detail = "unprocessable!!"))(ioInstance).value.unsafeRunSync().isRight)
    }

    "not be throw if isRight" in {
      val s = "a".toEitherF(x => x eqv "a")(UnprocessableEntity(detail = "unprocessable!!"))(ioInstance).andThrow.unsafeRunSync()
      assert(s eqv "a")
      val i = 1.toEitherF(x => x eqv 1)(UnprocessableEntity(detail = "unprocessable!!"))(ioInstance).andThrow.unsafeRunSync()
      assert(i eqv 1)
    }

    "be return left" in {
      assert("a".toEitherF(x => x =!= "a")(Unauthorized())(ioInstance).value.unsafeRunSync().isLeft)
      assert(1.toEitherF(x => x =!= 1)(UnprocessableEntity(detail = "unprocessable!!"))(ioInstance).value.unsafeRunSync().isLeft)
    }

    "be throw if isLeft" in {
      assertThrows[Unauthorized] {
        "a".toEitherF(x => x =!= "a")(Unauthorized())(ioInstance).andThrow.unsafeRunSync()
      }

      assertThrows[UnprocessableEntity] {
        1.toEitherF(x => x =!= 1)(UnprocessableEntity(detail = "unprocessable!!"))(ioInstance).andThrow.unsafeRunSync()
      }
    }
  }

}
