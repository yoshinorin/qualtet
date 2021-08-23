package net.yoshinorin.qualtet.domains.models.contents

import java.time.ZonedDateTime
import java.util.UUID
import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import net.yoshinorin.qualtet.domains.models.ResponseBase
import net.yoshinorin.qualtet.domains.models.authors.{AuthorId, AuthorName}
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeId

final case class ContentId(value: String = UUID.randomUUID().toString) extends AnyVal
object ContentId {
  implicit val encodeContentId: Encoder[ContentId] = deriveEncoder[ContentId]
  implicit val decodeContentId: Decoder[ContentId] = Decoder[String].map(ContentId.apply)

  def apply(value: String): ContentId = {
    // TODO: declare exception
    UUID.fromString(value)
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

final case class RequestContent(
  requestId: String = UUID.randomUUID().toString,
  authorName: AuthorName,
  contentType: String,
  path: Path,
  title: String,
  rawContent: String,
  htmlContent: Option[String] = None,
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
}

final case class ResponseContent(
  title: String,
  content: String,
  publishedAt: Long
) extends ResponseBase

object ResponseContent {
  implicit val encodeResponseContent: Encoder[ResponseContent] = deriveEncoder[ResponseContent]
  implicit val decodeResponseContent: Decoder[ResponseContent] = deriveDecoder[ResponseContent]
}
