package net.yoshinorin.qualtet.http.response

import cats.Applicative
import cats.effect.Concurrent
import org.http4s.dsl.Http4sDsl
import org.http4s.MediaType
import org.http4s.{Request, Response, Status}
import org.http4s.Challenge
import org.http4s.headers.{`Content-Type`, `WWW-Authenticate`}
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.http.errors.*
import net.yoshinorin.qualtet.http.errors.HttpError.*
import net.yoshinorin.qualtet.syntax.*

object Translator {

  // NOTE: can't use `ContextFunctions`.
  private[http] def failToResponse[F[_]: Concurrent](f: HttpError)(using req: Request[F])(using dsl: Http4sDsl[F]): F[Response[F]] = {
    import dsl.*
    f match {
      case e: NotFound =>
        dsl.NotFound(
          ResponseProblemDetails(
            title = e.title,
            status = dsl.NotFound.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: Unauthorized =>
        dsl.Unauthorized(`WWW-Authenticate`(Challenge("Bearer", "Unauthorized")))
      case e: UnprocessableContent =>
        dsl.UnprocessableContent(
          ResponseProblemDetails(
            title = e.title,
            status = dsl.UnprocessableContent.code,
            detail = e.detail,
            instance = req.uri.toString(),
            errors = e.errors
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: BadRequest =>
        dsl.BadRequest(
          ResponseProblemDetails(
            title = e.title,
            status = dsl.BadRequest.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: Forbidden =>
        dsl.Forbidden(
          ResponseProblemDetails(
            title = e.title,
            status = dsl.Forbidden.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: InternalServerError =>
        dsl.InternalServerError(
          ResponseProblemDetails(
            title = e.title,
            status = dsl.InternalServerError.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
    }
  }

  def toResponse[F[_]: Concurrent](e: Throwable)(using dsl: Http4sDsl[F]): Request[F] ?=> F[Response[F]] = {
    import dsl.*
    e match {
      case f: DomainError => this.failToResponse[F](fromDomainError(f))
      case _ => dsl.InternalServerError("Internal Server Error")
    }
  }

  def toResponse[F[_]: Applicative](status: Status, body: String): F[Response[F]] = {
    Applicative[F].pure(
      Response[F](status = status)
        .withEntity(body)
        .withContentType(`Content-Type`(MediaType.application.json))
    )
  }

  def toResponse[F[_]: Applicative, T](status: Status, body: T)(implicit e: JsonValueCodec[T]): F[Response[F]] = {
    Applicative[F].pure(
      Response[F](status = status)
        .withEntity(body.asJson)
        .withContentType(`Content-Type`(MediaType.application.json))
    )
  }

  // NOTE: can't use `using` or `ContextFunctions`.
  //       I don't know why can't use `using`...
  def toResponse[F[_]: Concurrent, T](a: Option[T])(implicit e: JsonValueCodec[T], req: Request[F], dsl: Http4sDsl[F]): F[Response[F]] = {
    import dsl.*
    a match {
      case Some(x) =>
        Ok(x.asJson, `Content-Type`(MediaType.application.json))
      case None =>
        dsl.NotFound(
          ResponseProblemDetails(
            title = "Not Found",
            status = dsl.NotFound.code,
            detail = "Not Found",
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
    }
  }

}
