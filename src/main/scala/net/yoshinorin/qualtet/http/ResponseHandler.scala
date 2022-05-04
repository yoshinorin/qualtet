package net.yoshinorin.qualtet.http

import io.circe.syntax._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCode}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import io.circe.Encoder
import net.yoshinorin.qualtet.error.Fail
import net.yoshinorin.qualtet.message.Message
import org.slf4j.LoggerFactory

trait ResponseHandler {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  private[this] def failToResponse(f: Fail): (StatusCode, Message) = {
    f match {
      case Fail.NotFound(message) => (NotFound, Message(message))
      case Fail.Unauthorized(message) => (Unauthorized, Message(message))
      case Fail.UnprocessableEntity(message) => (UnprocessableEntity, Message(message))
      case Fail.BadRequest(message) => (BadRequest, Message(message))
      case Fail.Forbidden(message) => (Forbidden, Message(message))
      case Fail.InternalServerError(message) => (InternalServerError, Message(message))
    }
  }

  private[this] def toFailureResponse(e: Exception): (StatusCode, Message) = {
    logger.error(e.getMessage)
    e match {
      case f: Fail => this.failToResponse(f)
      case _ => (InternalServerError, Message("Internal server error"))
    }
  }

  def httpResponse(e: Exception): StandardRoute = {
    val r = this.toFailureResponse(e)
    complete(HttpResponse(r._1, entity = HttpEntity(ContentTypes.`application/json`, r._2.asJson.toString())))
  }

  def httpResponse[T](statusCode: StatusCode, response: T)(implicit e: Encoder[T]): StandardRoute = {
    complete(HttpResponse(statusCode, entity = HttpEntity(ContentTypes.`application/json`, response.asJson.toString())))
  }

}
