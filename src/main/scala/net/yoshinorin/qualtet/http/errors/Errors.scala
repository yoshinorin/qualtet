package net.yoshinorin.qualtet.http.errors

import net.yoshinorin.qualtet.domains.errors.*

sealed trait HttpError extends Exception

final case class NotFound(
  title: String = "Not Found",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends HttpError

final case class Unauthorized(
  title: String = "Unauthorized",
  detail: String = "Unauthorized", // NOTE: no-need to set error details for secure reason. set default value instead.
  errors: Option[Seq[ProblemDetailsError]] = None
) extends HttpError

final case class UnprocessableEntity(
  title: String = "Unprocessable Entity",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends HttpError

final case class BadRequest(
  title: String = "Bad Request",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends HttpError

final case class Forbidden(
  title: String = "Forbidden",
  detail: String,
  errors: Option[Seq[ProblemDetailsError]] = None
) extends HttpError

final case class InternalServerError(
  title: String = "Internal Server Error",
  detail: String = "Internal Server Error"
) extends HttpError

object HttpError {
  def fromDomainError(e: DomainError): HttpError = {
    e match {
      case e: AuthorNotFound => NotFound(detail = e.detail, errors = e.errors)
      case e: ArticleNotFound => NotFound(detail = e.detail, errors = e.errors)
      case e: ContentNotFound => NotFound(detail = e.detail, errors = e.errors)
      case e: ContentTypeNotFound => NotFound(detail = e.detail, errors = e.errors)
      case e: SeriesNotFound => NotFound(detail = e.detail, errors = e.errors)
      case e: TagNotFound => NotFound(detail = e.detail, errors = e.errors)
      case e: net.yoshinorin.qualtet.domains.errors.Unauthorized => Unauthorized(detail = e.detail, errors = e.errors)
      case e: InvalidAuthorName => UnprocessableEntity(detail = e.detail, errors = e.errors)
      case e: InvalidAuthorDisplayName => UnprocessableEntity(detail = e.detail, errors = e.errors)
      case e: InvalidAttributes => UnprocessableEntity(detail = e.detail, errors = e.errors)
      case e: InvalidAuthor => UnprocessableEntity(detail = e.detail, errors = e.errors)
      case e: InvalidContentType => UnprocessableEntity(detail = e.detail, errors = e.errors)
      case e: InvalidExternalResourceKind => UnprocessableEntity(detail = e.detail, errors = e.errors)
      case e: InvalidPath => UnprocessableEntity(detail = e.detail, errors = e.errors)
      case e: InvalidSearchConditions => UnprocessableEntity(detail = e.detail, errors = e.errors)
      case e: InvalidSeries => UnprocessableEntity(detail = e.detail, errors = e.errors)
      case e: ContentTitleRequired => BadRequest(detail = e.detail, errors = e.errors)
      case e: RawContentRequired => BadRequest(detail = e.detail, errors = e.errors)
      case e: HtmlContentRequired => BadRequest(detail = e.detail, errors = e.errors)
      case e: SeriesNameRequired => BadRequest(detail = e.detail, errors = e.errors)
      case e: SeriesPathRequired => BadRequest(detail = e.detail, errors = e.errors)
      case e: SeriesTitleRequired => BadRequest(detail = e.detail, errors = e.errors)
      case e: UnexpectedJsonFormat => BadRequest(detail = e.detail, errors = e.errors)
      case e: UnexpectedException => InternalServerError(detail = e.detail)
    }
  }
}
