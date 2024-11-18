package net.yoshinorin.qualtet.domains.contentTypes

final case class ContentTypeReadModel(
  id: ContentTypeId = ContentTypeId.apply(),
  name: String
)
