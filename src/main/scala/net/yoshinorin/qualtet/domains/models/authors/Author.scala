package net.yoshinorin.qualtet.domains.models.authors

import java.time.ZonedDateTime
import java.util.UUID
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class Author(
  id: String = UUID.randomUUID().toString,
  name: String,
  displayName: String,
  password: String,
  createdAt: Long = ZonedDateTime.now.toEpochSecond
)

final case class ResponseAuthor(
  id: String = UUID.randomUUID().toString,
  name: String,
  displayName: String,
  createdAt: Long = ZonedDateTime.now.toEpochSecond
)

object ResponseAuthor {
  implicit val encodeAuthor: Encoder[ResponseAuthor] = deriveEncoder[ResponseAuthor]
  implicit val encodeAuthors: Encoder[List[ResponseAuthor]] = Encoder.encodeList[ResponseAuthor]
}
