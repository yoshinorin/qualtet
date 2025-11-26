package net.yoshinorin.qualtet.domains.contents

import java.time.ZonedDateTime
import cats.implicits.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.contents.ContentPath
import net.yoshinorin.qualtet.domains.Request
import net.yoshinorin.qualtet.domains.externalResources.ExternalResources
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.series.Series
import net.yoshinorin.qualtet.domains.tags.Tag
import net.yoshinorin.qualtet.domains.errors.{ContentTitleRequired, DomainError, HtmlContentRequired, RawContentRequired}
import net.yoshinorin.qualtet.syntax.*

final case class ContentRequestModel(
  contentType: String,
  robotsAttributes: Attributes, // TODO: Consider to use `Option[Attributes]`
  externalResources: List[ExternalResources] = List(),
  tags: List[Tag] = List(),
  series: Option[Series] = None,
  path: ContentPath,
  title: String,
  rawContent: String,
  htmlContent: String,
  publishedAt: Long = ZonedDateTime.now.toEpochSecond,
  updatedAt: Long = ZonedDateTime.now.toEpochSecond
) extends Request[ContentRequestModel] {
  // NOTE: see `net.yoshinorin.qualtet.domains.Request` comment.
  def postDecode: Either[DomainError, ContentRequestModel] = {
    for {
      decodedPath <- ContentPath(path.value)
      decodedTags <- tags.traverse(_.postDecode)
      decodedSeries <- series.traverse(_.postDecode)
      decodedTitle <- title.trimOrError(ContentTitleRequired(detail = "title required."))
      decodedRawContent <- rawContent.trimOrError(RawContentRequired(detail = "rawContent required."))
      decodedHtmlContent <- htmlContent.trimOrError(HtmlContentRequired(detail = "htmlContent required."))
    } yield new ContentRequestModel(
      contentType = contentType,
      robotsAttributes = this.robotsAttributes.sort,
      externalResources = externalResources,
      tags = decodedTags,
      series = decodedSeries,
      path = decodedPath,
      title = decodedTitle,
      rawContent = decodedRawContent,
      htmlContent = decodedHtmlContent,
      publishedAt = publishedAt,
      updatedAt = updatedAt
    )
  }
}

object ContentRequestModel {
  given codecRequestContent: JsonValueCodec[ContentRequestModel] = JsonCodecMaker.make
  given codecRequestContents: JsonValueCodec[List[ContentRequestModel]] = JsonCodecMaker.make

  def apply(
    contentType: String,
    robotsAttributes: Attributes,
    externalResources: List[ExternalResources] = List(),
    tags: List[Tag] = List(),
    series: Option[Series] = None,
    path: ContentPath,
    title: String,
    rawContent: String,
    htmlContent: String,
    publishedAt: Long = ZonedDateTime.now.toEpochSecond,
    updatedAt: Long = ZonedDateTime.now.toEpochSecond
  ): ContentRequestModel = {
    new ContentRequestModel(
      contentType = contentType,
      robotsAttributes = robotsAttributes,
      externalResources = externalResources,
      tags = tags,
      series = series,
      path = path,
      title = title.trimOrThrow(ContentTitleRequired(detail = "title required.")),
      rawContent = rawContent.trimOrThrow(RawContentRequired(detail = "rawContent required.")),
      htmlContent = htmlContent.trimOrThrow(HtmlContentRequired(detail = "htmlContent required.")),
      publishedAt = publishedAt,
      updatedAt = updatedAt
    )
  }
}
