package net.yoshinorin.qualtet.domains.errors

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

sealed trait DomainError extends Exception

final case class ProblemDetailsError(
  code: String,
  message: String
)

object ProblemDetailsError {
  given codecProblemDetailsError: JsonValueCodec[ProblemDetailsError] = JsonCodecMaker.make
  given codecProblemDetailsErrors: JsonValueCodec[Option[Seq[ProblemDetailsError]]] = JsonCodecMaker.make
}

final case class AuthorNotFound(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class ArticleNotFound(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class ContentNotFound(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class ContentTypeNotFound(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class SeriesNotFound(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class TagNotFound(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class Unauthorized(
  detail: String = "Unauthorized", // NOTE: no-need to set error details for secure reason. set default value instead.
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class InvalidAuthorName(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class InvalidAuthorDisplayName(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class InvalidAttributes(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class InvalidAuthor(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class InvalidContentType(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class InvalidExternalResourceKind(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class InvalidSearchConditions(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class InvalidSeries(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class ContentTitleRequired(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class RawContentRequired(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class HtmlContentRequired(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class SeriesNameRequired(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class SeriesTitleRequired(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class UnexpectedJsonFormat(
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends DomainError

final case class UnexpectedException(
  detail: String = "Internal Server Error"
) extends DomainError
