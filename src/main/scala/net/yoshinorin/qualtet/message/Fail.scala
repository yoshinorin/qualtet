package net.yoshinorin.qualtet.message

sealed trait Fail extends Exception

object Fail {

  final case class NotFound(message: String) extends Fail
  final case class Unauthorized(message: String = "Unauthorized") extends Fail
  final case class UnprocessableEntity(message: String) extends Fail
  final case class BadRequest(message: String) extends Fail
  final case class Forbidden(message: String = "Forbidden") extends Fail
  final case class InternalServerError(message: String = "Internal Server Error") extends Fail

}
