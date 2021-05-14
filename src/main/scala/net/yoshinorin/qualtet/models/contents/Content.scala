package net.yoshinorin.qualtet.models.contents

import java.time.ZonedDateTime
import java.util.UUID

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
