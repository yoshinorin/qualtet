package net.yoshinorin.qualtet.domains

import net.yoshinorin.qualtet.domains.errors.InvalidPath

import scala.util.matching.Regex

trait PathValidator {

  protected def validate(value: String): Either[InvalidPath, Unit] = {
    val normalized = normalize(value)
    if (normalized.codePointCount(0, normalized.length) > PathValidator.maxLength) {
      Left(InvalidPath(detail = s"Path must be ${PathValidator.maxLength} characters or less"))
    } else if (PathValidator.unusableChars.findFirstMatchIn(value).isDefined) {
      Left(InvalidPath(detail = s"Invalid character contains: ${value}"))
    } else if (PathValidator.invalidPercentRegex.matches(value)) {
      Left(InvalidPath(detail = s"Invalid percent encoding in path: ${value}"))
    } else {
      Right(())
    }
  }

  protected def normalize(value: String): String = {
    if (!value.startsWith("/")) s"/${value}" else value
  }
}

object PathValidator {
  // NOTE: Same as `VARCHAR(512)` of `path` columns (MariaDB counts characters, not bytes)
  private[domains] val maxLength: Int = 512
  private val unusableChars: Regex = "[:?#@!$&'()*+,;=<>\"\\\\^`{}|~]".r
  private val invalidPercentRegex: Regex = ".*%(?![0-9A-Fa-f]{2}).*".r
}
