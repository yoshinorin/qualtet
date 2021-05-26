package net.yoshinorin.qualtet.domains.models.contents

import java.time.ZonedDateTime
import java.util.UUID
import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder

final case class Content(
  id: String = UUID.randomUUID().toString,
  authorId: String,
  contentTypeId: String,
  path: String,
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
  id: String = UUID.randomUUID().toString,
  authorId: String,
  contentTypeId: String,
  path: String,
  title: String,
  rawContent: String,
  htmlContent: String,
  publishedAt: Long = ZonedDateTime.now.toEpochSecond,
  updatedAt: Long = ZonedDateTime.now.toEpochSecond
)

object RequestContent {

  implicit val encodeRequestContent: Encoder[RequestContent] = deriveEncoder[RequestContent]
  implicit val encodeRequestContents: Encoder[List[RequestContent]] = Encoder.encodeList[RequestContent]
  implicit val decodeRequestContent: Decoder[RequestContent] = deriveDecoder[RequestContent]
  implicit val decodeRequestContents: Decoder[List[RequestContent]] = Decoder.decodeList[RequestContent]

  /* TODO
  def apply(
    id: String,
    authorId: String,
    contentTypeId: String,
    path: String,
    title: String,
    rawContent: String,
    htmlContent: String,
    publishedAt: Long,
    updatedAt: Long
  ): RequestContent =
    new RequestContent(id, authorId, contentTypeId, path, title, rawContent, htmlContent, publishedAt, updatedAt)
 */
}
