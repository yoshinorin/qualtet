package net.yoshinorin.qualtet.message

sealed trait Fail extends Exception

object Fail {

  final case class NotFound(
    title: String = "Not Found",
    detail: String,
    errorCodes: Option[Seq[String]] = None
  ) extends Fail

  final case class Unauthorized(
    title: String = "Unauthorized",
    detail: String = "Unauthorized", // NOTE: no-need to set error details for secure reason. set default value instead.
    errorCodes: Option[Seq[String]] = None
  ) extends Fail

  final case class UnprocessableEntity(
    title: String = "Unprocessable Entity",
    detail: String,
    errorCodes: Option[Seq[String]] = None
  ) extends Fail

  final case class BadRequest(
    title: String = "Bad Request",
    detail: String,
    errorCodes: Option[Seq[String]] = None
  ) extends Fail

  final case class Forbidden(
    title: String = "Forbidden",
    detail: String,
    errorCodes: Option[Seq[String]] = None
  ) extends Fail

  final case class InternalServerError(
    title: String = "Internal Server Error",
    detail: String = "Internal Server Error"
  ) extends Fail

}
