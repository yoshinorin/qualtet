package net.yoshinorin.qualtet.http

import cats.effect.IO
import org.http4s.dsl.io.*
import org.http4s.MediaType
import org.http4s.{Request, Response}
import org.http4s.Challenge
import org.http4s.headers.{`Content-Type`, `WWW-Authenticate`}
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.errors.{
  BadRequest => DomainBadRequest,
  Error,
  Forbidden => DomainForbidden,
  InternalServerError => DomainInternalServerError,
  NotFound => DomainNotFound,
  Unauthorized => DomainUnauthorized,
  UnprocessableEntity => DomainUnprocessableEntity
}
import net.yoshinorin.qualtet.syntax.*

object ResponseTranslator {

  // NOTE: can't use `ContextFunctions`.
  private def failToResponse(f: Error)(using req: Request[IO]): IO[Response[IO]] = {
    f match {
      case e: DomainNotFound =>
        NotFound(
          ResponseProblemDetails(
            title = e.title,
            status = NotFound.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: DomainUnauthorized =>
        Unauthorized(`WWW-Authenticate`(Challenge("Bearer", "Unauthorized")))
      case e: DomainUnprocessableEntity =>
        UnprocessableEntity(
          ResponseProblemDetails(
            title = e.title,
            status = UnprocessableEntity.code,
            detail = e.detail,
            instance = req.uri.toString(),
            errors = e.errors
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: DomainBadRequest =>
        BadRequest(
          ResponseProblemDetails(
            title = e.title,
            status = BadRequest.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: DomainForbidden =>
        Forbidden(
          ResponseProblemDetails(
            title = e.title,
            status = Forbidden.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: DomainInternalServerError =>
        InternalServerError(
          ResponseProblemDetails(
            title = e.title,
            status = InternalServerError.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
    }
  }

  def toResponse(e: Throwable): Request[IO] ?=> IO[Response[IO]] = {
    e match {
      case f: Error => this.failToResponse(f)
      case _ => InternalServerError("Internal Server Error")
    }
  }

  // NOTE: can't use `using` or `ContextFunctions`.
  //       I don't know why can't use `using`...
  def toResponse[T](a: Option[T])(implicit e: JsonValueCodec[T], req: Request[IO]): IO[Response[IO]] = {
    a match {
      case Some(x) =>
        Ok(x.asJson, `Content-Type`(MediaType.application.json))
      case None =>
        NotFound(
          ResponseProblemDetails(
            title = "Not Found",
            status = NotFound.code,
            detail = "Not Found",
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
    }
  }

}
