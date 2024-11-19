package net.yoshinorin.qualtet.domains.authors

final case class AuthorReadModel(
  id: AuthorId,
  name: AuthorName,
  displayName: AuthorDisplayName,
  password: BCryptPassword,
  createdAt: Long
)

final case class AuthorWithoutPasswordReadModel(
  id: AuthorId,
  name: AuthorName,
  displayName: AuthorDisplayName,
  createdAt: Long
)
