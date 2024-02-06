package net.yoshinorin.qualtet.http

import cats.effect.IO
import org.http4s.dsl.io.*
import org.http4s.MediaType
import org.http4s.Response
import org.http4s.Challenge
import org.http4s.headers.{`Content-Type`, `WWW-Authenticate`}
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.message.Message
import net.yoshinorin.qualtet.syntax.*

object ResponseTranslator {

  private[this] def failToResponse(f: Fail): IO[Response[IO]] = {
    f match {
      case e: Fail.NotFound => NotFound(Message(e.detail).asJson, `Content-Type`(MediaType.application.json))
      case e: Fail.Unauthorized => Unauthorized(`WWW-Authenticate`(Challenge("Bearer", "Unauthorized")))
      case e: Fail.UnprocessableEntity => UnprocessableEntity(Message(e.detail).asJson, `Content-Type`(MediaType.application.json))
      case e: Fail.BadRequest => BadRequest(Message(e.detail).asJson, `Content-Type`(MediaType.application.json))
      case e: Fail.Forbidden => Forbidden(Message(e.detail).asJson, `Content-Type`(MediaType.application.json))
      case e: Fail.InternalServerError => InternalServerError(Message(e.title).asJson, `Content-Type`(MediaType.application.json))
    }
  }

  def toResponse(e: Throwable): IO[Response[IO]] = {
    e match {
      case f: Fail => this.failToResponse(f)
      case _ => InternalServerError("Internal Server Error")
    }
  }

  def toResponse[T](a: Option[T])(implicit e: JsonValueCodec[T]): IO[Response[IO]] = {
    a match {
      case Some(x) => Ok(x.asJson, `Content-Type`(MediaType.application.json))
      case None => NotFound(Message("Not Found").asJson, `Content-Type`(MediaType.application.json))
    }
  }

}
