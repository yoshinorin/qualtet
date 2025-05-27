package net.yoshinorin.qualtet.infrastructure.versions

enum MigrationStatus(val value: String) {
  case NOT_REQUIRED extends MigrationStatus("not_required")
  case UNAPPLIED extends MigrationStatus("unapplied")
  case IN_PROGRESS extends MigrationStatus("in_progress")
  case SUCCESS extends MigrationStatus("success")
  case FAILED extends MigrationStatus("failed")
}

final case class Version(
  version: String,
  migrationStatus: MigrationStatus = MigrationStatus.NOT_REQUIRED,
  deployedAt: Long = 0
)
