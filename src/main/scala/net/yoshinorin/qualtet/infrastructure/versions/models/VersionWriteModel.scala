package net.yoshinorin.qualtet.infrastructure.versions

final case class VersionWriteModel(
  version: String,
  migrationStatus: MigrationStatus,
  deployedAt: Long
)
