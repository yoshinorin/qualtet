package net.yoshinorin.qualtet.syntax

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import net.yoshinorin.qualtet.fixture.{error, unsafe}
import net.yoshinorin.qualtet.domains.errors.{ContentNotFound, UnexpectedException}
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.syntax.MonadSpec
class MonadSpec extends AnyWordSpec {

  "monad syntax" should {

    "errorIfNone" should {

      "return IO[Right] if Some" in {
        val ioOption: IO[Option[String]] = IO.pure(Some("test value"))
        val result = ioOption.errorIfNone(UnexpectedException("not found")).unsafeRunSync()

        assert(result.isRight)
        assert(result.unsafe === "test value")
      }

      "return IO[Left] if None" in {
        val ioOption: IO[Option[String]] = IO.pure(None)
        val result = ioOption.errorIfNone(UnexpectedException("not found")).unsafeRunSync()

        assert(result.isLeft)
        assert(result.error.isInstanceOf[UnexpectedException])
        assert(result.error.asInstanceOf[UnexpectedException].detail === "not found")
      }

      "work with different error types" in {
        val ioOption: IO[Option[Int]] = IO.pure(None)
        val result = ioOption.errorIfNone(ContentNotFound(detail = "content not found")).unsafeRunSync()

        assert(result.isLeft)
        assert(result.error.isInstanceOf[ContentNotFound])
        assert(result.error.asInstanceOf[ContentNotFound].detail === "content not found")
      }

      "preserve value type" in {
        case class TestData(id: Int, name: String)
        val testData = TestData(1, "test")
        val ioOption: IO[Option[TestData]] = IO.pure(Some(testData))
        val result = ioOption.errorIfNone(UnexpectedException("not found")).unsafeRunSync()

        assert(result.isRight)
        assert(result.unsafe === testData)
        assert(result.unsafe.id === 1)
        assert(result.unsafe.name === "test")
      }

    }

  }

}
