package net.yoshinorin.qualtet.infrastructure.versions

final case class VersionReadModel(
  version: VersionString,
  migrationStatus: MigrationStatus,
  deployedAt: Long
)
