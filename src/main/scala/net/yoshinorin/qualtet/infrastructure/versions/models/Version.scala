package net.yoshinorin.qualtet.infrastructure.versions

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

  def apply(value: String): VersionString = {
    if (value.isEmpty() || !VersionPattern.matches(value)) {
      throw new IllegalArgumentException(
        s"Invalid version format: $value. Version must contain only digits and dots, where dots can only appear between digits."
      )
    }
    value
  }

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
