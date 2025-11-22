package net.yoshinorin.qualtet.infrastructure.versions

import net.yoshinorin.qualtet.domains.errors.InvalidVersion

enum MigrationStatus(val value: String) {
  case NOT_REQUIRED extends MigrationStatus("not_required")
  case UNAPPLIED extends MigrationStatus("unapplied")
  case IN_PROGRESS extends MigrationStatus("in_progress")
  case SUCCESS extends MigrationStatus("success")
  case FAILED extends MigrationStatus("failed")
}

opaque type VersionString = String

object VersionString {

  import cats.Eq

  /*  Allow examples:
        - `1`
        - `1.2`
        - `1.2.3.4`

      Disallow examples:
        - `.1`
        - `1.`
        - `1a`
        - `1.2.3.4.5`
   */
  private val VersionPattern = "^\\d+(\\.\\d+){0,3}$".r

  def apply(value: String): Either[InvalidVersion, VersionString] = {
    if (value.isEmpty() || !VersionPattern.matches(value)) {
      Left(
        InvalidVersion(
          detail = s"Invalid version format: $value. Version must contain only digits and dots, where dots can only appear between digits."
        )
      )
    } else {
      Right(value)
    }
  }

  /**
   * Create a VersionString from a trusted source (e.g., database) without validation.
   *
   * This method should ONLY be used in Repository layer when reading data from the database.
   * Database data is assumed to be already validated at write time, so we skip validation
   * for performance reasons.
   *
   * DO NOT use this method in:
   * - HTTP request handlers
   * - User input processing
   * - Any external data source
   *
   * @param value The raw string value from a trusted source
   * @return The VersionString without validation
   */
  private[versions] def unsafe(value: String): VersionString = value

  given eq: Eq[VersionString] = Eq.instance { (a, b) =>
    a.value == b.value
  }

  extension (vs: VersionString) {
    def value: String = vs
  }
}

final case class Version(
  version: VersionString,
  migrationStatus: MigrationStatus = MigrationStatus.NOT_REQUIRED,
  deployedAt: Long = 0
)
