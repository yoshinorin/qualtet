package net.yoshinorin.qualtet.domains.authors

final case class AuthorWriteModel(
  id: AuthorId,
  name: AuthorName,
  displayName: AuthorDisplayName,
  password: BCryptPassword,
  createdAt: Long
)
