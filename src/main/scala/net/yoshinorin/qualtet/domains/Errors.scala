package net.yoshinorin.qualtet.domains.errors

import net.yoshinorin.qualtet.http.ProblemDetailsError

sealed trait Error extends Exception

final case class AuthorNotFound(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class ArticleNotFound(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class ContentNotFound(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class ContentTypeNotFound(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class SeriesNotFound(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class TagNotFound(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class Unauthorized(
  detail: String = "Unauthorized", // NOTE: no-need to set error details for secure reason. set default value instead.
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidAuthorName(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidAuthorDisplayName(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidAttributes(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidAuthor(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidContentType(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidExternalResourceKind(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidSearchConditions(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class InvalidSeries(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class ContentTitleRequired(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class RawContentRequired(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class HtmlContentRequired(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class SeriesNameRequired(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class SeriesTitleRequired(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class UnexpectedJsonFormat(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends Error

final case class UnexpectedException(
  detail: String = "Internal Server Error"
) extends Error
