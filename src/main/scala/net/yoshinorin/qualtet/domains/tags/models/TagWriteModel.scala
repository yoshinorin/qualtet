package net.yoshinorin.qualtet.domains.tags

final case class TagWriteModel(
  id: TagId,
  name: TagName,
  path: TagPath
)
