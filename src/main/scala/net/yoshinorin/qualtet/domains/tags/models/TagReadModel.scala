package net.yoshinorin.qualtet.domains.tags

final case class TagReadModel(
  id: TagId,
  name: TagName
)

final case class TagWithCountReadModel(
  id: TagId,
  name: TagName,
  count: Int
)
