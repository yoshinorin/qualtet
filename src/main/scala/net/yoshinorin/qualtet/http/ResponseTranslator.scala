package net.yoshinorin.qualtet.http

import cats.effect.IO
import org.http4s.dsl.io.*
import org.http4s.MediaType
import org.http4s.{Request, Response}
import org.http4s.Challenge
import org.http4s.headers.{`Content-Type`, `WWW-Authenticate`}
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.message.ProblemDetails
import net.yoshinorin.qualtet.syntax.*

object ResponseTranslator {

  // NOTE: can't use `ContextFunctions`.
  private[this] def failToResponse(f: Fail)(using req: Request[IO]): IO[Response[IO]] = {
    f match {
      case e: Fail.NotFound =>
        NotFound(
          ProblemDetails(
            title = e.title,
            status = NotFound.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: Fail.Unauthorized =>
        Unauthorized(`WWW-Authenticate`(Challenge("Bearer", "Unauthorized")))
      case e: Fail.UnprocessableEntity =>
        UnprocessableEntity(
          ProblemDetails(
            title = e.title,
            status = UnprocessableEntity.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: Fail.BadRequest =>
        BadRequest(
          ProblemDetails(
            title = e.title,
            status = BadRequest.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: Fail.Forbidden =>
        Forbidden(
          ProblemDetails(
            title = e.title,
            status = Forbidden.code,
            detail = e.detail,
            instance = req.uri.toString()
          ).asJson,
          `Content-Type`(MediaType.application.`problem+json`)
        )
      case e: Fail.InternalServerError =>
        InternalServerError(
          ProblemDetails(
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
      case f: Fail => this.failToResponse(f)
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
          ProblemDetails(
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
