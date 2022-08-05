package net.yoshinorin.qualtet.domains.contents

import java.time.ZonedDateTime
import wvlet.airframe.ulid.ULID
import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import net.yoshinorin.qualtet.domains.authors.{AuthorId, AuthorName}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.externalResources.ExternalResources
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.tags.{Tag, TagId}
import net.yoshinorin.qualtet.message.Fail.BadRequest
import net.yoshinorin.qualtet.syntax._
import java.util.Locale

final case class ContentId(value: String = ULID.newULIDString.toLowerCase(Locale.ENGLISH)) extends AnyVal
object ContentId {
  implicit val encodeContentId: Encoder[ContentId] = Encoder[String].contramap(_.value)
  implicit val decodeContentId: Decoder[ContentId] = Decoder[String].map(ContentId.apply)

  def apply(value: String): ContentId = {
    ULID.fromString(value)
    new ContentId(value)
  }
}

final case class Path(value: String) extends AnyVal
object Path {
  implicit val encodePath: Encoder[Path] = Encoder[String].contramap(_.value)
  implicit val decodePath: Decoder[Path] = Decoder[String].map(Path.apply)

  def apply(value: String): Path = {
    // TODO: check valid url https://www.ietf.org/rfc/rfc3986.txt
    if (!value.startsWith("/")) {
      new Path(s"/${value}")
    } else {
      new Path(value)
    }
  }
}

final case class Content(
  id: ContentId = new ContentId,
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
  implicit val encodeContent: Encoder[Content] = deriveEncoder[Content]
  implicit val encodeContents: Encoder[List[Content]] = Encoder.encodeList[Content]
  implicit val decodeContent: Decoder[Content] = deriveDecoder[Content]
  implicit val decodeContents: Decoder[List[Content]] = Decoder.decodeList[Content]
}

final case class ContentTagging(
  ContentId: ContentId,
  TagId: TagId
)

final case class RequestContent(
  requestId: String = ULID.newULIDString.toLowerCase(Locale.ENGLISH),
  contentType: String,
  robotsAttributes: Attributes, // TODO: change to Option[Attributes]
  externalResources: Option[List[ExternalResources]] = None,
  tags: Option[List[String]] = None,
  path: Path,
  title: String,
  rawContent: String,
  htmlContent: String,
  publishedAt: Long = ZonedDateTime.now.toEpochSecond,
  updatedAt: Long = ZonedDateTime.now.toEpochSecond
)

object RequestContent {

  import io.circe.generic.extras.Configuration

  implicit val circeCustomConfig: Configuration = Configuration.default.withDefaults

  implicit val encodeRequestContent: Encoder[RequestContent] = deriveEncoder[RequestContent]
  implicit val encodeRequestContents: Encoder[List[RequestContent]] = Encoder.encodeList[RequestContent]
  implicit val decodeRequestContent: Decoder[RequestContent] = deriveConfiguredDecoder
  implicit val decodeRequestContents: Decoder[List[RequestContent]] = Decoder.decodeList[RequestContent]

  def apply(
    requestId: String = ULID.newULIDString.toLowerCase(Locale.ENGLISH),
    contentType: String,
    robotsAttributes: Attributes,
    externalResources: Option[List[ExternalResources]] = None,
    tags: Option[List[String]] = None,
    path: Path,
    title: String,
    rawContent: String,
    htmlContent: String,
    publishedAt: Long = ZonedDateTime.now.toEpochSecond,
    updatedAt: Long = ZonedDateTime.now.toEpochSecond
  ): RequestContent = {
    new RequestContent(
      requestId = requestId,
      contentType = contentType,
      robotsAttributes = robotsAttributes,
      externalResources = externalResources,
      tags = tags,
      path = path,
      title = title.trimOrThrow(BadRequest("title required.")),
      rawContent = rawContent.trimOrThrow(BadRequest("rawContent required.")),
      htmlContent = htmlContent.trimOrThrow(BadRequest("htmlContent required.")),
      publishedAt = publishedAt,
      updatedAt = updatedAt
    )
  }
}

final case class ResponseContent(
  title: String,
  robotsAttributes: Attributes,
  externalResources: Option[List[ExternalResources]] = None,
  tags: Option[List[Tag]] = None,
  description: String,
  content: String,
  authorName: AuthorName,
  publishedAt: Long,
  updatedAt: Long
)

object ResponseContent {
  implicit val encodeResponseContent: Encoder[ResponseContent] = deriveEncoder[ResponseContent]
  implicit val decodeResponseContent: Decoder[ResponseContent] = deriveDecoder[ResponseContent]
}

final case class ResponseContentDbRow(
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
