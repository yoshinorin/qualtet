package net.yoshinorin.qualtet.domains.models.authors

import java.time.ZonedDateTime
import java.util.UUID
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class Author(
  id: String = UUID.randomUUID().toString,
  name: String,
  displayName: String,
  createdAt: Long = ZonedDateTime.now.toEpochSecond
)

object Author {
  implicit val encodeAuthor: Encoder[Author] = deriveEncoder[Author]
  implicit val encodeAuthors: Encoder[List[Author]] = Encoder.encodeList[Author]
}
