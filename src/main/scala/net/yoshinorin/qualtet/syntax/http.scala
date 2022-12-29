package net.yoshinorin.qualtet.syntax

import cats.effect.IO
import org.http4s.Response
import net.yoshinorin.qualtet.http.ResponseTranslator
import com.github.plokhotnyuk.jsoniter_scala.core._

trait http {

  implicit final class ResponseOps(e: Throwable) {
    def asResponse: IO[Response[IO]] = {
      ResponseTranslator.toResponse(e)
    }
  }

  implicit final class OptionalResponseOps[T](a: Option[T]) {
    def asResponse(implicit e: JsonValueCodec[T]): IO[Response[IO]] = {
      ResponseTranslator.toResponse[T](a)
    }
  }

}
