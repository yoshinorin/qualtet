package net.yoshinorin.qualtet.syntax

import cats.effect.IO
import org.http4s.Response
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import net.yoshinorin.qualtet.http.ResponseTranslator

trait http {

  extension (e: Throwable) {
    def asResponse: IO[Response[IO]] = {
      ResponseTranslator.toResponse(e)
    }
  }

  extension (e: IO[Throwable]) {
    def andResponse: IO[Response[IO]] = {
      e.flatMap(_.asResponse)
    }
  }

  extension [T](a: Option[T]) {
    def asResponse(implicit e: JsonValueCodec[T]): IO[Response[IO]] = {
      ResponseTranslator.toResponse[T](a)
    }
  }

}
