package net.yoshinorin.qualtet.domains.authors

import java.time.ZonedDateTime
import wvlet.airframe.ulid.ULID
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.ValueExtender
import net.yoshinorin.qualtet.message.Fail.{Unauthorized, UnprocessableEntity}
import net.yoshinorin.qualtet.syntax.*

import scala.util.matching.Regex

opaque type AuthorId = String
object AuthorId extends ValueExtender[AuthorId] {
  given codecAuthorId: JsonValueCodec[AuthorId] = JsonCodecMaker.make

  def apply(value: String = ULID.newULIDString.toLower): AuthorId = {
    val _ = ULID.fromString(value)
    value.toLower
  }
}

opaque type AuthorName = String
object AuthorName extends ValueExtender[AuthorName] {
  given codecAuthorName: JsonValueCodec[AuthorName] = JsonCodecMaker.make
  val authorNamePattern: Regex = "[0-9a-zA-Z_-]+".r

  def apply(value: String): AuthorName = {
    if (!authorNamePattern.matches(value)) {
      throw UnprocessableEntity("authorName must be number, alphabet and underscore.")
    }
    value.toLower
  }
}

opaque type AuthorDisplayName = String
object AuthorDisplayName extends ValueExtender[AuthorDisplayName] {
  given codecAuthorDisplayName: JsonValueCodec[AuthorDisplayName] = JsonCodecMaker.make
  val authorDisplayNamePattern: Regex = "[0-9a-zA-Z_-]+".r

  def apply(value: String): AuthorDisplayName = {
    if (!authorDisplayNamePattern.matches(value)) {
      throw UnprocessableEntity("authorDisplayName must be number, alphabet or underscore.")
    }
    value
  }
}

opaque type BCryptPassword = String
object BCryptPassword extends ValueExtender[BCryptPassword] {
  def apply(value: String): BCryptPassword = {
    // https://docs.spring.io/spring-security/site/docs/current/reference/html5/#authentication-password-storage-dpe
    if (!value.startsWith("$2a$")) {
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

final case class ResponseAuthor(
  id: AuthorId = AuthorId.apply(),
  name: AuthorName,
  displayName: AuthorDisplayName,
  createdAt: Long = ZonedDateTime.now.toEpochSecond
)

object ResponseAuthor {
  given codecAuthor: JsonValueCodec[ResponseAuthor] = JsonCodecMaker.make
  given codecAuthors: JsonValueCodec[Seq[ResponseAuthor]] = JsonCodecMaker.make
}
