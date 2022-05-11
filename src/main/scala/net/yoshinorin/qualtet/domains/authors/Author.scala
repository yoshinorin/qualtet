package net.yoshinorin.qualtet.domains.authors

import java.time.ZonedDateTime
import wvlet.airframe.ulid.ULID
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.deriveEncoder
import net.yoshinorin.qualtet.message.Fail.{Unauthorized, UnprocessableEntity}

import scala.util.matching.Regex

final case class AuthorId(value: String = ULID.newULIDString.toLowerCase) extends AnyVal
object AuthorId {
  implicit val encodeAuthorId: Encoder[AuthorId] = Encoder[String].contramap(_.value)
  implicit val decodeAuthorId: Decoder[AuthorId] = Decoder[String].map(AuthorId.apply)

  def apply(value: String): AuthorId = {
    ULID.fromString(value)
    new AuthorId(value)
  }
}

final case class AuthorName(value: String) extends AnyVal
object AuthorName {
  implicit val encodeAuthorName: Encoder[AuthorName] = Encoder[String].contramap(_.value)
  implicit val decodeAuthorName: Decoder[AuthorName] = Decoder[String].map(AuthorName.apply)
  val authorNamePattern: Regex = "[0-9a-zA-Z_-]+".r

  def apply(value: String): AuthorName = {
    if (!authorNamePattern.matches(value)) {
      throw UnprocessableEntity("authorName must be number, alphabet and underscore.")
    }
    new AuthorName(value.toLowerCase)
  }
}

final case class AuthorDisplayName(value: String) extends AnyVal
object AuthorDisplayName {
  implicit val encodeAuthorDisplayName: Encoder[AuthorDisplayName] = Encoder[String].contramap(_.value)
  implicit val decodeAuthorDisplayName: Decoder[AuthorDisplayName] = Decoder[String].map(AuthorDisplayName.apply)
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
  implicit val encodeAuthor: Encoder[ResponseAuthor] = deriveEncoder[ResponseAuthor]
  implicit val encodeAuthors: Encoder[List[ResponseAuthor]] = Encoder.encodeList[ResponseAuthor]
}
