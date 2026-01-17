package net.yoshinorin.qualtet.syntax

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.wordspec.AnyWordSpec
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jFactory
import net.yoshinorin.qualtet.syntax.*

// testOnly net.yoshinorin.qualtet.syntax.LoggerSpec
class LoggerSpec extends AnyWordSpec with logger {

  given logger: SelfAwareStructuredLogger[IO] = Slf4jFactory.create[IO].getLogger

  "logger syntax" should {

    "logLeft" should {

      "return the same Left value when Left with Debug level" in {
        val originalError = new RuntimeException("test error")
        val either: Either[RuntimeException, String] = Left(originalError)
        val result = either.logLeft[IO](Debug).unsafeRunSync()

        assert(result === Left(originalError))
      }

      "return the same Left value when Left with Info level" in {
        val originalError = new RuntimeException("test error")
        val either: Either[RuntimeException, String] = Left(originalError)
        val result = either.logLeft[IO](Info).unsafeRunSync()

        assert(result === Left(originalError))
      }

      "return the same Left value when Left with Warn level" in {
        val originalError = new RuntimeException("test error")
        val either: Either[RuntimeException, String] = Left(originalError)
        val result = either.logLeft[IO](Warn).unsafeRunSync()

        assert(result === Left(originalError))
      }

      "return the same Left value when Left with Error level" in {
        val originalError = new RuntimeException("test error")
        val either: Either[RuntimeException, String] = Left(originalError)
        val result = either.logLeft[IO](Error).unsafeRunSync()

        assert(result === Left(originalError))
      }

      "return the same Right value when Right" in {
        val either: Either[RuntimeException, String] = Right("success value")
        val result = either.logLeft[IO](Error).unsafeRunSync()

        assert(result === Right("success value"))
      }

      "work with different error types" in {
        val originalError = new IllegalArgumentException("invalid argument")
        val either: Either[IllegalArgumentException, Int] = Left(originalError)
        val result = either.logLeft[IO](Error).unsafeRunSync()

        assert(result === Left(originalError))
      }

      "work with different value types" in {
        case class TestData(id: Int, name: String)
        val testData = TestData(1, "test")
        val either: Either[Exception, TestData] = Right(testData)
        val result = either.logLeft[IO](Info).unsafeRunSync()

        assert(result === Right(testData))
      }

    }

  }

}
