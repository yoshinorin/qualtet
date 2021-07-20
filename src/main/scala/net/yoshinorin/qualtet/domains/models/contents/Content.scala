package net.yoshinorin.qualtet.domains.models.contents

import java.time.ZonedDateTime
import java.util.UUID
import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

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
  requestId: String = UUID.randomUUID().toString,
  author: String,
  contentType: String,
  path: String,
  title: String,
  rawContent: String,
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
)

object ResponseContent {
  implicit val encodeResponseContent: Encoder[ResponseContent] = deriveEncoder[ResponseContent]
  implicit val decodeResponseContent: Decoder[ResponseContent] = deriveDecoder[ResponseContent]
}
