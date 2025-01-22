package net.yoshinorin.qualtet.syntax

import cats.effect.IO
import org.http4s.{Request, Response}
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import net.yoshinorin.qualtet.domains.{Limit, Order, Page, PaginationRequestModel}
import net.yoshinorin.qualtet.http.response.Translator

import scala.util.Try

trait http {

  extension (e: Throwable) {
    def asResponse: Request[IO] ?=> IO[Response[IO]] = {
      Translator.toResponse(e)
    }
  }

  extension (e: IO[Throwable]) {
    def andResponse: Request[IO] ?=> IO[Response[IO]] = {
      e.flatMap(_.asResponse)
    }
  }

  extension [T](a: Option[T]) {
    def asResponse: (JsonValueCodec[T], Request[IO]) ?=> IO[Response[IO]] = {
      Translator.toResponse[T](a)
    }
  }

  extension (q: Map[String, String]) {
    def asPagination: PaginationRequestModel = {
      val a = Try(q.getOrElse("page", 1).toString.trim.toInt)
      val b = Try(q.getOrElse("limit", 10).toString.trim.toInt)
      val o = Try(Order.valueOf(q.getOrElse("order", "desc").toUpperCase()))

      PaginationRequestModel(Some(Page(a.getOrElse(1))), Some(Limit(b.getOrElse(10))), Some(o.getOrElse(Order.DESC)))
    }
  }

}
