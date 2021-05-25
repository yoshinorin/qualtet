package net.yoshinorin.qualtet.domains.models.contents

import java.time.ZonedDateTime
import java.util.UUID
import io.circe.Encoder
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
}
