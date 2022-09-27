package net.yoshinorin.qualtet.domains.contents

import java.time.ZonedDateTime
import wvlet.airframe.ulid.ULID
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.domains.Request
import net.yoshinorin.qualtet.domains.authors.{AuthorId, AuthorName}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.externalResources.ExternalResources
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.tags.Tag
import net.yoshinorin.qualtet.message.Fail.BadRequest
import net.yoshinorin.qualtet.syntax._
import java.util.Locale

final case class ContentId(value: String = ULID.newULIDString.toLowerCase(Locale.ENGLISH)) extends AnyVal
object ContentId {
  implicit val codecContentId: JsonValueCodec[ContentId] = JsonCodecMaker.make

  def apply(value: String): ContentId = {
    val _ = ULID.fromString(value)
    new ContentId(value)
  }
}

final case class Path(value: String) extends AnyVal
object Path {
  implicit val codecPath: JsonValueCodec[Path] = JsonCodecMaker.make

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
  implicit val codecContent: JsonValueCodec[Content] = JsonCodecMaker.make
  implicit val codecContents: JsonValueCodec[List[Content]] = JsonCodecMaker.make
}

final case class RequestContent(
  requestId: String = ULID.newULIDString.toLowerCase(Locale.ENGLISH),
  contentType: String,
  robotsAttributes: Attributes, // TODO: change to Option[Attributes]
  externalResources: List[ExternalResources] = List(),
  tags: List[String] = List(),
  path: Path,
  title: String,
  rawContent: String,
  htmlContent: String,
  publishedAt: Long = ZonedDateTime.now.toEpochSecond,
  updatedAt: Long = ZonedDateTime.now.toEpochSecond
) extends Request[RequestContent] {
  def postDecode: RequestContent = {
    this.title.trimOrThrow(BadRequest("title required."))
    this.rawContent.trimOrThrow(BadRequest("rawContent required."))
    this.htmlContent.trimOrThrow(BadRequest("htmlContent required."))

    val robotsAttributes = new Attributes(
      value = this.robotsAttributes.value
    )
    println(robotsAttributes)

    this.copy(
      /*
      externalResources = this.externalResources.map(er =>
          new ExternalResources(
          kind = er..externalResources.kind,
          name = this.externalResources.name
        )
      ),
       */
      robotsAttributes = new Attributes(
        value = this.robotsAttributes.value
      )
    )
  }
}

object RequestContent {
  implicit val codecRequestContent: JsonValueCodec[RequestContent] = JsonCodecMaker.make
  implicit val codecRequestContents: JsonValueCodec[List[RequestContent]] = JsonCodecMaker.make

  def apply(
    requestId: String = ULID.newULIDString.toLowerCase(Locale.ENGLISH),
    contentType: String,
    robotsAttributes: Attributes,
    externalResources: List[ExternalResources] = List(),
    tags: List[String] = List(),
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
  externalResources: List[ExternalResources] = List(),
  tags: List[Tag] = List(),
  description: String,
  content: String,
  authorName: AuthorName,
  publishedAt: Long,
  updatedAt: Long
)

object ResponseContent {
  implicit val codecResponseContent: JsonValueCodec[ResponseContent] = JsonCodecMaker.make(
    CodecMakerConfig
      .withRequireCollectionFields(true)
      .withTransientEmpty(false)
      .withSkipNestedOptionValues(false)
      .withSkipUnexpectedFields(false)
      .withTransientEmpty(false)
      .withTransientDefault(false)
  )
  implicit val codecResponseContents: JsonValueCodec[Seq[ResponseContent]] = JsonCodecMaker.make
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
