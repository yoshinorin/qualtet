package net.yoshinorin.qualtet.syntax

import cats.effect.IO
import org.http4s.Response
import net.yoshinorin.qualtet.http.ResponseTranslator

trait http {

  implicit final class ResponseOps(e: Throwable) {
    def asResponse: IO[Response[IO]] = {
      ResponseTranslator.toResponse(e)
    }
  }

  implicit final class ResponseIOOps(e: IO[Throwable]) {
    def andResponse: IO[Response[IO]] = {
      e.flatMap(_.asResponse)
    }
  }

  implicit final class OptionalResponseOps[T](a: Option[T]) {
    import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec

    def asResponse(implicit e: JsonValueCodec[T]): IO[Response[IO]] = {
      ResponseTranslator.toResponse[T](a)
    }
  }

}
