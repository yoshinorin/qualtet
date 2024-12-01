package net.yoshinorin.qualtet.domains.errors

import net.yoshinorin.qualtet.http.ProblemDetailsError

sealed trait Error extends Exception

final case class AuthorNotFound(
  title: String = "Not Found",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class ArticleNotFound(
  title: String = "Not Found",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class ContentNotFound(
  title: String = "Not Found",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class ContentTypeNotFound(
  title: String = "Not Found",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class SeriesNotFound(
  title: String = "Not Found",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class TagNotFound(
  title: String = "Not Found",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class Unauthorized(
  title: String = "Unauthorized",
  detail: String = "Unauthorized", // NOTE: no-need to set error details for secure reason. set default value instead.
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidAuthorName(
  title: String = "Unprocessable Entity",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidAuthorDisplayName(
  title: String = "Unprocessable Entity",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidAttributes(
  title: String = "Unprocessable Entity",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidAuthor(
  title: String = "Unprocessable Entity",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidContentType(
  title: String = "Unprocessable Entity",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidExternalResourceKind(
  title: String = "Unprocessable Entity",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidSearchConditions(
  title: String = "Unprocessable Entity",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidSeries(
  title: String = "Unprocessable Entity",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class ContentTitleRequired(
  title: String = "Bad Request",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class RawContentRequired(
  title: String = "Bad Request",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class HtmlContentRequired(
  title: String = "Bad Request",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class SeriesNameRequired(
  title: String = "Bad Request",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class SeriesTitleRequired(
  title: String = "Bad Request",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class UnexpectedJsonFormat(
  title: String = "Bad Request",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class UnexpectedException(
  title: String = "Internal Server Error",
  detail: String = "Internal Server Error"
) extends Error
