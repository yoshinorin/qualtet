package net.yoshinorin.qualtet.models.authors

import java.time.ZonedDateTime
import java.util.UUID

final case class Author(
  id: String = UUID.randomUUID().toString,
  name: String,
  displayName: String,
  createdAt: Long = ZonedDateTime.now.toEpochSecond
)
