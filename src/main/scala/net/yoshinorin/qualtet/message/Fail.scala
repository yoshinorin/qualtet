package net.yoshinorin.qualtet.message

import net.yoshinorin.qualtet.http.ProblemDetailsError

sealed trait Fail extends Exception

object Fail {

  final case class NotFound(
    title: String = "Not Found",
    detail: String,
    errors: Option[Seq[ProblemDetailsError]] = None
  ) extends Fail

  final case class Unauthorized(
    title: String = "Unauthorized",
    detail: String = "Unauthorized", // NOTE: no-need to set error details for secure reason. set default value instead.
    errors: Option[Seq[ProblemDetailsError]] = None
  ) extends Fail

  final case class UnprocessableEntity(
    title: String = "Unprocessable Entity",
    detail: String,
    errors: Option[Seq[ProblemDetailsError]] = None
  ) extends Fail

  final case class BadRequest(
    title: String = "Bad Request",
    detail: String,
    errors: Option[Seq[ProblemDetailsError]] = None
  ) extends Fail

  final case class Forbidden(
    title: String = "Forbidden",
    detail: String,
    errors: Option[Seq[ProblemDetailsError]] = None
  ) extends Fail

  final case class InternalServerError(
    title: String = "Internal Server Error",
    detail: String = "Internal Server Error"
  ) extends Fail

}
