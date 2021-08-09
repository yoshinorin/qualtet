package net.yoshinorin.qualtet.domains.models.authors

import java.time.ZonedDateTime
import java.util.UUID
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.deriveEncoder

import scala.util.matching.Regex

final case class AuthorId(value: String = UUID.randomUUID().toString) extends AnyVal
object AuthorId {
  implicit val encodeAuthorId: Encoder[AuthorId] = Encoder[String].contramap(_.value)
  implicit val decodeAuthorId: Decoder[AuthorId] = Decoder[String].map(AuthorId.apply)

  def apply(value: String): AuthorId = {
    // TODO: declare exception
    UUID.fromString(value)
    new AuthorId(value)
  }
}

final case class AuthorName(value: String) extends AnyVal
object AuthorName {
  implicit val encodeUserName: Encoder[AuthorName] = Encoder[String].contramap(_.value)
  implicit val decodeUserName: Decoder[AuthorName] = Decoder[String].map(AuthorName.apply)
  val authorNamePattern: Regex = "[0-9a-zA-Z_-]+".r

  def apply(value: String): AuthorName = {
    if (!authorNamePattern.matches(value)) {
      // TODO: declare exception
      throw new Exception("TODO")
    }
    new AuthorName(value.toLowerCase)
  }
}

final case class AuthorDisplayName(value: String) extends AnyVal
object AuthorDisplayName {
  implicit val encodeUserName: Encoder[AuthorDisplayName] = Encoder[String].contramap(_.value)
  implicit val decodeUserName: Decoder[AuthorDisplayName] = Decoder[String].map(AuthorDisplayName.apply)
  val authorDisplayNamePattern: Regex = "[0-9a-zA-Z_-]+".r

  def apply(value: String): AuthorDisplayName = {
    if (!authorDisplayNamePattern.matches(value)) {
      // TODO: declare exception
      throw new Exception("TODO")
    }
    new AuthorDisplayName(value)
  }
}

final case class Author(
  id: AuthorId = new AuthorId,
  name: AuthorName,
  displayName: AuthorDisplayName,
  password: String,
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
