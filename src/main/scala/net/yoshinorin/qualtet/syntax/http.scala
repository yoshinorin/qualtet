package net.yoshinorin.qualtet.syntax

import cats.effect.IO
import org.http4s.Response
import net.yoshinorin.qualtet.http.ResponseTranslator

trait http {

  implicit final class ResponseOps(e: Throwable) {
    def asResponse: IO[Response[IO]] = {
      ResponseTranslator.toFailureResponse(e)
    }
  }

}
