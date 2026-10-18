package net.yoshinorin.qualtet.domains

import net.yoshinorin.qualtet.domains.errors.InvalidPath

import scala.util.matching.Regex

trait PathValidator {

  protected def validate(value: String): Either[InvalidPath, Unit] = {
    if (PathValidator.unusableChars.findFirstMatchIn(value).isDefined) {
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
  private val unusableChars: Regex = "[:?#@!$&'()*+,;=<>\"\\\\^`{}|~]".r
  private val invalidPercentRegex: Regex = ".*%(?![0-9A-Fa-f]{2}).*".r
}
