package net.yoshinorin.qualtet.domains.authors

import java.time.ZonedDateTime
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{FromTrustedSource, UlidConvertible, ValueExtender}
import net.yoshinorin.qualtet.domains.errors.{InvalidAuthorDisplayName, InvalidAuthorName, Unauthorized}
import net.yoshinorin.qualtet.syntax.*

import scala.util.matching.Regex

opaque type AuthorId = String
object AuthorId extends ValueExtender[AuthorId] with UlidConvertible[AuthorId] {
  given codecAuthorId: JsonValueCodec[AuthorId] = JsonCodecMaker.make
}

opaque type AuthorName = String
object AuthorName extends ValueExtender[AuthorName] {
  given codecAuthorName: JsonValueCodec[AuthorName] = JsonCodecMaker.make
  val authorNamePattern: Regex = "[0-9a-zA-Z_-]+".r

  def apply(value: String): Either[InvalidAuthorName, AuthorName] = {
    if (!authorNamePattern.matches(value)) {
      Left(InvalidAuthorName(detail = "authorName must be number, alphabet and underscore."))
    } else {
      Right(value.toLower)
    }
  }

  private def unsafeFrom(value: String): AuthorName = value.toLower

  given fromTrustedSource: FromTrustedSource[AuthorName] with {
    def fromTrusted(value: String): AuthorName = unsafeFrom(value)
  }
}

opaque type AuthorDisplayName = String
object AuthorDisplayName extends ValueExtender[AuthorDisplayName] {
  given codecAuthorDisplayName: JsonValueCodec[AuthorDisplayName] = JsonCodecMaker.make
  val authorDisplayNamePattern: Regex = "[0-9a-zA-Z_-]+".r

  def apply(value: String): Either[InvalidAuthorDisplayName, AuthorDisplayName] = {
    if (!authorDisplayNamePattern.matches(value)) {
      Left(InvalidAuthorDisplayName(detail = "authorDisplayName must be number, alphabet or underscore."))
    } else {
      Right(value)
    }
  }

  private def unsafeFrom(value: String): AuthorDisplayName = value

  given fromTrustedSource: FromTrustedSource[AuthorDisplayName] with {
    def fromTrusted(value: String): AuthorDisplayName = unsafeFrom(value)
  }
}

opaque type BCryptPassword = String
object BCryptPassword extends ValueExtender[BCryptPassword] {
  def apply(value: String): BCryptPassword = {
    // https://docs.spring.io/spring-security/site/docs/current/reference/html5/#authentication-password-storage-dpe
    if (!value.startsWith("$2a$")) {
      // TODO: Throw to Either
      throw Unauthorized()
    }
    value
  }
}

final case class Author(
  id: AuthorId = AuthorId.apply(),
  name: AuthorName,
  displayName: AuthorDisplayName,
  password: BCryptPassword,
  createdAt: Long = ZonedDateTime.now.toEpochSecond
)
