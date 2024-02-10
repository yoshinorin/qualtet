package net.yoshinorin.qualtet.syntax

import cats.effect.IO
import org.http4s.{Request, Response}
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import net.yoshinorin.qualtet.http.{RequestQueryParamater, ResponseTranslator}

import scala.util.Try

trait http {

  extension (e: Throwable) {
    def asResponse: Request[IO] ?=> IO[Response[IO]] = {
      ResponseTranslator.toResponse(e)
    }
  }

  extension (e: IO[Throwable]) {
    def andResponse: Request[IO] ?=> IO[Response[IO]] = {
      e.flatMap(_.asResponse)
    }
  }

  extension [T](a: Option[T]) {
    def asResponse: (JsonValueCodec[T], Request[IO]) ?=> IO[Response[IO]] = {
      ResponseTranslator.toResponse[T](a)
    }
  }

  extension (q: Map[String, String]) {
    def asRequestQueryParamater: RequestQueryParamater = {
      val a = Try(q.getOrElse("page", 1).toString.trim.toInt)
      val b = Try(q.getOrElse("limit", 10).toString.trim.toInt)

      RequestQueryParamater(Some(a.getOrElse(1)), Some(b.getOrElse(10)))
    }
  }

}
