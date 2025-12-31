package net.yoshinorin.qualtet.http.response

import cats.effect.IO
import org.http4s.dsl.io.*
import org.http4s.MediaType
import org.http4s.{Request, Response}
import org.http4s.Challenge
import org.http4s.headers.{`Content-Type`, `WWW-Authenticate`}
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.http.errors.*
import net.yoshinorin.qualtet.http.errors.HttpError.*
import net.yoshinorin.qualtet.syntax.*

object Translator {

  // NOTE: can't use `ContextFunctions`.
  private[http] def failToResponse(f: HttpError)(using req: Request[IO]): IO[Response[IO]] = {
    f match {
      case e: NotFound =>
        org.http4s.dsl.io.NotFound(
          ResponseProblemDetails(
            title = e.title,
            status = org.http4s.dsl.io.NotFound.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: Unauthorized =>
        org.http4s.dsl.io.Unauthorized(`WWW-Authenticate`(Challenge("Bearer", "Unauthorized")))
      case e: UnprocessableEntity =>
        org.http4s.dsl.io.UnprocessableContent(
          ResponseProblemDetails(
            title = e.title,
            status = org.http4s.dsl.io.UnprocessableContent.code,
            detail = e.detail,
            instance = req.uri.toString(),
            errors = e.errors
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: BadRequest =>
        org.http4s.dsl.io.BadRequest(
          ResponseProblemDetails(
            title = e.title,
            status = org.http4s.dsl.io.BadRequest.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: Forbidden =>
        org.http4s.dsl.io.Forbidden(
          ResponseProblemDetails(
            title = e.title,
            status = org.http4s.dsl.io.Forbidden.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: InternalServerError =>
        org.http4s.dsl.io.InternalServerError(
          ResponseProblemDetails(
            title = e.title,
            status = org.http4s.dsl.io.InternalServerError.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
    }
  }

  def toResponse(e: Throwable): Request[IO] ?=> IO[Response[IO]] = {
    e match {
      case f: DomainError => this.failToResponse(fromDomainError(f))
      case _ => org.http4s.dsl.io.InternalServerError("Internal Server Error")
    }
  }

  // NOTE: can't use `using` or `ContextFunctions`.
  //       I don't know why can't use `using`...
  def toResponse[T](a: Option[T])(implicit e: JsonValueCodec[T], req: Request[IO]): IO[Response[IO]] = {
    a match {
      case Some(x) =>
        Ok(x.asJson, `Content-Type`(MediaType.application.json))
      case None =>
        org.http4s.dsl.io.NotFound(
          ResponseProblemDetails(
            title = "Not Found",
            status = org.http4s.dsl.io.NotFound.code,
            detail = "Not Found",
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
    }
  }

}
