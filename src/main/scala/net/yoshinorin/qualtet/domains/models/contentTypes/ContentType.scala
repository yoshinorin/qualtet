package net.yoshinorin.qualtet.domains.models.contentTypes

import java.util.UUID

final case class ContentType(
  id: String = UUID.randomUUID().toString,
  name: String
)
