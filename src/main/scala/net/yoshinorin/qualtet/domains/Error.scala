package net.yoshinorin.qualtet.domains.errors

import net.yoshinorin.qualtet.http.ProblemDetailsError

sealed trait Error extends Exception

final case class NotFound(
  title: String = "Not Found",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class Unauthorized(
  title: String = "Unauthorized",
  detail: String = "Unauthorized", // NOTE: no-need to set error details for secure reason. set default value instead.
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class UnprocessableEntity(
  title: String = "Unprocessable Entity",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class BadRequest(
  title: String = "Bad Request",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class Forbidden(
  title: String = "Forbidden",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InternalServerError(
  title: String = "Internal Server Error",
  detail: String = "Internal Server Error"
) extends Error
