package net.yoshinorin.qualtet.syntax

import cats.Monad
import cats.effect.Concurrent
import org.http4s.{Request, Response, Status}
import org.http4s.dsl.Http4sDsl
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import net.yoshinorin.qualtet.domains.{Limit, Order, Page, PaginationRequestModel}
import net.yoshinorin.qualtet.http.response.Translator

import scala.util.Try

trait http {

  extension [F[_]](request: Request[F]) {
    def path: String = {
      request.uri.path.toString()
    }
  }

  extension (e: Throwable) {
    def asResponse[F[_]: Concurrent](using Http4sDsl[F]): Request[F] ?=> F[Response[F]] = {
      Translator.toResponse[F](e)
    }
  }

  extension [F[_]: Concurrent](e: F[Throwable])(using Http4sDsl[F]) {
    def asResponse: Request[F] ?=> F[Response[F]] = {
      Monad[F].flatMap(e)(_.asResponse[F])
    }
  }

  extension [T](a: Option[T]) {
    def asResponse[F[_]: Concurrent](using Http4sDsl[F]): (JsonValueCodec[T], Request[F]) ?=> F[Response[F]] = {
      Translator.toResponse[F, T](a)
    }
  }

  extension (body: String) {
    def asResponse[F[_]: Concurrent](status: Status)(using Http4sDsl[F]): F[Response[F]] = {
      Translator.toResponse[F](status, body)
    }
  }

  extension [T](body: T) {
    def asResponse[F[_]: Concurrent](status: Status)(using Http4sDsl[F]): (JsonValueCodec[T]) ?=> F[Response[F]] = {
      Translator.toResponse[F, T](status, body)
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
