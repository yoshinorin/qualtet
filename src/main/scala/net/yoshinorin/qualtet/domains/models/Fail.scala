package net.yoshinorin.qualtet.domains.models

import io.circe.Encoder
import io.circe.generic.semiauto._

sealed trait Fail extends Exception

object Fail {

  implicit val encodeFail: Encoder[Fail] = deriveEncoder[Fail]

  case class NotFound(message: String) extends Fail
  case class Unauthorized(message: String) extends Fail
  case class UnprocessableEntity(message: String) extends Fail
  case class BadRequest(message: String) extends Fail
  case object Forbidden extends Fail
  case object InternalServerError extends Fail

}
