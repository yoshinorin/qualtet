package net.yoshinorin.qualtet.syntax

import cats.effect.IO
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.message.Fail.UnprocessableEntity

// testOnly net.yoshinorin.qualtet.syntax.IoSpec
class IoSpec extends AnyWordSpec {

  "io syntax" should {

    "not be throw if not None" in {
      val i: String => IO[Option[String]] = a => IO(Some(a))
      assert(i("something").throwIfNone(UnprocessableEntity("unprocessable!!")).unsafeRunSync() === "something")
    }

    "be thrown if None" in {
      val i: Option[String] => IO[Option[String]] = _ => IO(None)
      assertThrows[UnprocessableEntity] {
        i(None).throwIfNone(UnprocessableEntity("unprocessable!!")).unsafeRunSync()
      }
    }

  }

}
