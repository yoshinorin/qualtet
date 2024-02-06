package net.yoshinorin.qualtet.domains.contents

import java.time.ZonedDateTime
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{Request, ValueExtender, UlidConvertible}
import net.yoshinorin.qualtet.domains.authors.{AuthorId, AuthorName}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.externalResources.ExternalResources
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.series.SeriesName
import net.yoshinorin.qualtet.domains.tags.Tag
import net.yoshinorin.qualtet.message.Fail.BadRequest
import net.yoshinorin.qualtet.syntax.*

opaque type ContentId = String
object ContentId extends ValueExtender[ContentId] with UlidConvertible[ContentId] {
  given codecContentId: JsonValueCodec[ContentId] = JsonCodecMaker.make
}

// TODO: move somewhere
opaque type Path = String
object Path extends ValueExtender[Path] {
  given codecPath: JsonValueCodec[Path] = JsonCodecMaker.make

  def apply(value: String): Path = {
    // TODO: check valid url https://www.ietf.org/rfc/rfc3986.txt
    if (!value.startsWith("/")) {
      s"/${value}"
    } else {
      value
    }
  }
}

final case class Content(
  id: ContentId = ContentId.apply(),
  authorId: AuthorId,
  contentTypeId: ContentTypeId,
  path: Path,
  title: String,
  rawContent: String,
  htmlContent: String,
  publishedAt: Long = ZonedDateTime.now.toEpochSecond,
  updatedAt: Long = ZonedDateTime.now.toEpochSecond
)

object Content {
  given codecContent: JsonValueCodec[Content] = JsonCodecMaker.make
  given codecContents: JsonValueCodec[List[Content]] = JsonCodecMaker.make
}

final case class RequestContent(
  contentType: String,
  robotsAttributes: Attributes, // TODO: Consider to use `Option[Attributes]`
  externalResources: List[ExternalResources] = List(),
  tags: List[String] = List(),
  series: Option[SeriesName] = None,
  path: Path,
  title: String,
  rawContent: String,
  htmlContent: String,
  publishedAt: Long = ZonedDateTime.now.toEpochSecond,
  updatedAt: Long = ZonedDateTime.now.toEpochSecond
) extends Request[RequestContent] {
  // NOTE: see `net.yoshinorin.qualtet.domains.Request` comment.
  def postDecode: RequestContent = {
    new RequestContent(
      contentType = contentType,
      robotsAttributes = this.robotsAttributes.sort,
      externalResources = externalResources,
      tags = tags,
      series = series,
      path = path,
      title = title.trimOrThrow(BadRequest(detail = "title required.")),
      rawContent = rawContent.trimOrThrow(BadRequest(detail = "rawContent required.")),
      htmlContent = htmlContent.trimOrThrow(BadRequest(detail = "htmlContent required.")),
      publishedAt = publishedAt,
      updatedAt = updatedAt
    )
  }
}

object RequestContent {
  given codecRequestContent: JsonValueCodec[RequestContent] = JsonCodecMaker.make
  given codecRequestContents: JsonValueCodec[List[RequestContent]] = JsonCodecMaker.make

  def apply(
    contentType: String,
    robotsAttributes: Attributes,
    externalResources: List[ExternalResources] = List(),
    tags: List[String] = List(),
    series: Option[SeriesName] = None,
    path: Path,
    title: String,
    rawContent: String,
    htmlContent: String,
    publishedAt: Long = ZonedDateTime.now.toEpochSecond,
    updatedAt: Long = ZonedDateTime.now.toEpochSecond
  ): RequestContent = {
    new RequestContent(
      contentType = contentType,
      robotsAttributes = robotsAttributes,
      externalResources = externalResources,
      tags = tags,
      series = series,
      path = path,
      title = title.trimOrThrow(BadRequest(detail = "title required.")),
      rawContent = rawContent.trimOrThrow(BadRequest(detail = "rawContent required.")),
      htmlContent = htmlContent.trimOrThrow(BadRequest(detail = "htmlContent required.")),
      publishedAt = publishedAt,
      updatedAt = updatedAt
    )
  }
}

final case class ResponseContent(
  id: ContentId,
  title: String,
  robotsAttributes: Attributes,
  externalResources: List[ExternalResources] = List(),
  tags: List[Tag] = List(),
  description: String,
  content: String,
  length: Int,
  authorName: AuthorName,
  publishedAt: Long,
  updatedAt: Long
)

object ResponseContent {
  given codecResponseContent: JsonValueCodec[ResponseContent] = JsonCodecMaker.make(
    CodecMakerConfig
      .withRequireCollectionFields(true)
      .withTransientEmpty(false)
      .withSkipNestedOptionValues(false)
      .withSkipUnexpectedFields(false)
      .withTransientEmpty(false)
      .withTransientDefault(false)
  )
  given codecResponseContents: JsonValueCodec[Seq[ResponseContent]] = JsonCodecMaker.make
}
final case class ResponseContentDbRow(
  id: ContentId,
  title: String,
  robotsAttributes: Attributes,
  externalResourceKindKeys: Option[String],
  externalResourceKindValues: Option[String],
  tagIds: Option[String],
  tagNames: Option[String],
  content: String,
  authorName: AuthorName,
  publishedAt: Long,
  updatedAt: Long
)
