package net.yoshinorin.qualtet.message

import io.circe.Encoder
import io.circe.generic.semiauto._

sealed trait Fail extends Exception

object Fail {

  implicit val encodeFail: Encoder[Fail] = deriveEncoder[Fail]

  final case class NotFound(message: String) extends Fail
  final case class Unauthorized(message: String = "Unauthorized") extends Fail
  final case class UnprocessableEntity(message: String) extends Fail
  final case class BadRequest(message: String) extends Fail
  final case class Forbidden(message: String = "Forbidden") extends Fail
  final case class InternalServerError(message: String = "Internal Server Error") extends Fail

}
