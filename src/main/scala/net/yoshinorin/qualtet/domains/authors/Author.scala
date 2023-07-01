package net.yoshinorin.qualtet.domains.authors

import java.time.ZonedDateTime
import wvlet.airframe.ulid.ULID
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.message.Fail.{Unauthorized, UnprocessableEntity}
import net.yoshinorin.qualtet.syntax.*

import scala.util.matching.Regex

final case class AuthorId(value: String = ULID.newULIDString.toLower) extends AnyVal
object AuthorId {
  given codecAuthorId: JsonValueCodec[AuthorId] = JsonCodecMaker.make

  def apply(value: String): AuthorId = {
    val _ = ULID.fromString(value)
    new AuthorId(value)
  }
}

final case class AuthorName(value: String) extends AnyVal
object AuthorName {
  given codecAuthorName: JsonValueCodec[AuthorName] = JsonCodecMaker.make
  val authorNamePattern: Regex = "[0-9a-zA-Z_-]+".r

  def apply(value: String): AuthorName = {
    if (!authorNamePattern.matches(value)) {
      throw UnprocessableEntity("authorName must be number, alphabet and underscore.")
    }
    new AuthorName(value.toLower)
  }
}

final case class AuthorDisplayName(value: String) extends AnyVal
object AuthorDisplayName {
  given codecAuthorDisplayName: JsonValueCodec[AuthorDisplayName] = JsonCodecMaker.make
  val authorDisplayNamePattern: Regex = "[0-9a-zA-Z_-]+".r

  def apply(value: String): AuthorDisplayName = {
    if (!authorDisplayNamePattern.matches(value)) {
      throw UnprocessableEntity("authorDisplayName must be number, alphabet or underscore.")
    }
    new AuthorDisplayName(value)
  }
}

final case class BCryptPassword(value: String) extends AnyVal
object BCryptPassword {
  def apply(value: String): BCryptPassword = {
    // https://docs.spring.io/spring-security/site/docs/current/reference/html5/#authentication-password-storage-dpe
    if (!value.startsWith("$2a$")) {
      throw Unauthorized()
    }
    new BCryptPassword(value)
  }
}

final case class Author(
  id: AuthorId = new AuthorId,
  name: AuthorName,
  displayName: AuthorDisplayName,
  password: BCryptPassword,
  createdAt: Long = ZonedDateTime.now.toEpochSecond
)

final case class ResponseAuthor(
  id: AuthorId = new AuthorId,
  name: AuthorName,
  displayName: AuthorDisplayName,
  createdAt: Long = ZonedDateTime.now.toEpochSecond
)

object ResponseAuthor {
  given codecAuthor: JsonValueCodec[ResponseAuthor] = JsonCodecMaker.make
  given codecAuthors: JsonValueCodec[Seq[ResponseAuthor]] = JsonCodecMaker.make
}
