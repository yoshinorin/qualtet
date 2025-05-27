package net.yoshinorin.qualtet.infrastructure.versions

final case class VersionReadModel(
  version: String,
  migrationStatus: MigrationStatus,
  deployedAt: Long
)
