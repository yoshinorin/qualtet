package net.yoshinorin.qualtet.http.errors

import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.errors.*

class ErrorsSpec extends AnyWordSpec {

  "HttpError" should {

    "convert DomainError to HttpError correctly" in {
      val authorNotFound = AuthorNotFound(detail = "Author not found")
      assert(HttpError.fromDomainError(authorNotFound) === NotFound(detail = "Author not found"))

      val articleNotFound = ArticleNotFound(detail = "Article not found")
      assert(HttpError.fromDomainError(articleNotFound) === NotFound(detail = "Article not found"))

      val contentNotFound = ContentNotFound(detail = "Content not found")
      assert(HttpError.fromDomainError(contentNotFound) === NotFound(detail = "Content not found"))

      val contentTypeNotFound = ContentTypeNotFound(detail = "Content type not found")
      assert(HttpError.fromDomainError(contentTypeNotFound) === NotFound(detail = "Content type not found"))

      val seriesNotFound = SeriesNotFound(detail = "Series not found")
      assert(HttpError.fromDomainError(seriesNotFound) === NotFound(detail = "Series not found"))

      val tagNotFound = TagNotFound(detail = "Tag not found")
      assert(HttpError.fromDomainError(tagNotFound) === NotFound(detail = "Tag not found"))

      val unauthorized = Unauthorized(detail = "Unauthorized")
      assert(HttpError.fromDomainError(unauthorized) === net.yoshinorin.qualtet.http.errors.Unauthorized(detail = "Unauthorized"))

      val invalidAuthorName = InvalidAuthorName(detail = "Invalid author name")
      assert(HttpError.fromDomainError(invalidAuthorName) === UnprocessableEntity(detail = "Invalid author name"))

      val invalidAuthorDisplayName = InvalidAuthorDisplayName(detail = "Invalid author display name")
      assert(HttpError.fromDomainError(invalidAuthorDisplayName) === UnprocessableEntity(detail = "Invalid author display name"))

      val invalidAttributes = InvalidAttributes(detail = "Invalid attributes")
      assert(HttpError.fromDomainError(invalidAttributes) === UnprocessableEntity(detail = "Invalid attributes"))

      val invalidAuthor = InvalidAuthor(detail = "Invalid author")
      assert(HttpError.fromDomainError(invalidAuthor) === UnprocessableEntity(detail = "Invalid author"))

      val invalidContentType = InvalidContentType(detail = "Invalid content type")
      assert(HttpError.fromDomainError(invalidContentType) === UnprocessableEntity(detail = "Invalid content type"))

      val invalidExternalResourceKind = InvalidExternalResourceKind(detail = "Invalid external resource kind")
      assert(HttpError.fromDomainError(invalidExternalResourceKind) === UnprocessableEntity(detail = "Invalid external resource kind"))

      val invalidPath = InvalidPath(detail = "Invalid path")
      assert(HttpError.fromDomainError(invalidPath) === UnprocessableEntity(detail = "Invalid path"))

      val invalidSearchConditions = InvalidSearchConditions(detail = "Invalid search conditions")
      assert(HttpError.fromDomainError(invalidSearchConditions) === UnprocessableEntity(detail = "Invalid search conditions"))

      val invalidSeries = InvalidSeries(detail = "Invalid series")
      assert(HttpError.fromDomainError(invalidSeries) === UnprocessableEntity(detail = "Invalid series"))

      val contentTitleRequired = ContentTitleRequired(detail = "Content title required")
      assert(HttpError.fromDomainError(contentTitleRequired) === BadRequest(detail = "Content title required"))

      val rawContentRequired = RawContentRequired(detail = "Raw content required")
      assert(HttpError.fromDomainError(rawContentRequired) === BadRequest(detail = "Raw content required"))

      val htmlContentRequired = HtmlContentRequired(detail = "HTML content required")
      assert(HttpError.fromDomainError(htmlContentRequired) === BadRequest(detail = "HTML content required"))

      val seriesNameRequired = SeriesNameRequired(detail = "Series name required")
      assert(HttpError.fromDomainError(seriesNameRequired) === BadRequest(detail = "Series name required"))

      val seriesPathRequired = SeriesPathRequired(detail = "Series path required")
      assert(HttpError.fromDomainError(seriesPathRequired) === BadRequest(detail = "Series path required"))

      val seriesTitleRequired = SeriesTitleRequired(detail = "Series title required")
      assert(HttpError.fromDomainError(seriesTitleRequired) === BadRequest(detail = "Series title required"))

      val unexpectedJsonFormat = UnexpectedJsonFormat(detail = "Unexpected JSON format")
      assert(HttpError.fromDomainError(unexpectedJsonFormat) === BadRequest(detail = "Unexpected JSON format"))

      val unexpectedException = UnexpectedException(detail = "Unexpected exception")
      assert(HttpError.fromDomainError(unexpectedException) === InternalServerError(detail = "Unexpected exception"))
    }
  }
}
