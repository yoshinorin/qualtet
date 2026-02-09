package net.yoshinorin.qualtet.http.response

import cats.effect.IO
import org.http4s.{Request, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.*
import net.yoshinorin.qualtet.http.errors.*
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.response.TranslatorSpec
class TranslatorSpec extends AnyWordSpec {

  given Http4sDsl[IO] = Http4sDsl[IO]
  given testRoute: Request[IO] = Request[IO](uri = uri"/test")

  "failToReponse" should {
    "convert BadRequest HttpError to 400 response" in {
      val error = BadRequest("bad request", "invalid")
      (for {
        response <- Translator.failToResponse(error)
      } yield assert(response.status.code == 400)).unsafeRunSync()
    }

    "convert Unauthorized HttpError to 401 response" in {
      val error = Unauthorized("unauthorized access")
      (for {
        response <- Translator.failToResponse(error)
      } yield assert(response.status.code == 401)).unsafeRunSync()
    }

    "convert Forbidden HttpError to 403 response" in {
      val error = Forbidden("forbidden access", "invalid")
      (for {
        response <- Translator.failToResponse(error)
      } yield assert(response.status.code == 403)).unsafeRunSync()
    }

    "convert NotFound HttpError to 404 response" in {
      val error = NotFound("test resource", "invalid")
      (for {
        response <- Translator.failToResponse(error)
      } yield assert(response.status.code == 404)).unsafeRunSync()
    }

    "convert UnprocessableContent HttpError to 422 response" in {
      val error = UnprocessableContent("invalid data", "invalid")
      (for {
        response <- Translator.failToResponse(error)
      } yield assert(response.status.code == 422)).unsafeRunSync()
    }

    "convert InternalServerError HttpError to 500 response" in {
      val error = InternalServerError("internal error")
      (for {
        response <- Translator.failToResponse(error)
      } yield assert(response.status.code == 500)).unsafeRunSync()
    }
  }

  "toResponse" should {
    "convert Not DomainError exceptions to 500 response" in {
      val exception = new RuntimeException("unexpected error")
      (for {
        response <- Translator.toResponse[IO](exception)
      } yield assert(response.status.code == 500)).unsafeRunSync()
    }
  }

}
