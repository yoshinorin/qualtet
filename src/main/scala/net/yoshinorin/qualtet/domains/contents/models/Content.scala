package net.yoshinorin.qualtet.domains.contents

import java.time.ZonedDateTime
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{FromTrustedSource, UlidConvertible, ValueExtender}
import net.yoshinorin.qualtet.domains.authors.{AuthorId, AuthorName}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.errors.InvalidPath

opaque type ContentId = String
object ContentId extends ValueExtender[ContentId] with UlidConvertible[ContentId] {
  given codecContentId: JsonValueCodec[ContentId] = JsonCodecMaker.make
}

opaque type ContentPath = String
object ContentPath extends ValueExtender[ContentPath] {
  given codecPath: JsonValueCodec[ContentPath] = JsonCodecMaker.make

  private lazy val unusableChars = "[:?#@!$&'()*+,;=<>\"\\\\^`{}|~]"
  private lazy val invalidPercentRegex = ".*%(?![0-9A-Fa-f]{2}).*"

  private val RESERVED_PATHS: Set[String] = Set(
    "adjacent",
    "admin",
    "api",
    "assets",
    "health",
    "metrics",
    "navigation",
    "public",
    "recommendations",
    "related",
    "static",
    "system"
  )

  private def isReserved(path: String): Boolean = {
    path.split("/").filter(_.nonEmpty).exists(segment => RESERVED_PATHS.contains(segment.toLowerCase))
  }

  def apply(value: String): Either[InvalidPath, ContentPath] = {

    if (unusableChars.r.findFirstMatchIn(value).isDefined) {
      return Left(InvalidPath(detail = s"Invalid character contains: ${value}"))
    }
    if (invalidPercentRegex.r.matches(value)) {
      return Left(InvalidPath(detail = s"Invalid percent encoding in path: ${value}"))
    }
    if (isReserved(value)) {
      return Left(InvalidPath(detail = s"Path contains reserved word: ${value}"))
    }
    val normalized = if (!value.startsWith("/")) s"/${value}" else value
    Right(normalized)
  }

  private def unsafeFrom(value: String): ContentPath = {
    if (!value.startsWith("/")) s"/${value}" else value
  }

  given fromTrustedSource: FromTrustedSource[ContentPath] with {
    def fromTrusted(value: String): ContentPath = unsafeFrom(value)
  }
}

final case class Content(
  id: ContentId = ContentId.apply(),
  authorId: AuthorId,
  contentTypeId: ContentTypeId,
  path: ContentPath,
  title: String,
  rawContent: String,
  htmlContent: String,
  publishedAt: Long = ZonedDateTime.now.toEpochSecond,
  updatedAt: Long = ZonedDateTime.now.toEpochSecond
)

final case class ContentWithMeta(
  id: ContentId,
  title: String,
  robotsAttributes: Attributes,
  externalResourceKindKeys: Option[String],
  externalResourceKindValues: Option[String],
  tagIds: Option[String],
  tagNames: Option[String],
  tagPaths: Option[String],
  content: String,
  authorName: AuthorName,
  publishedAt: Long = ZonedDateTime.now.toEpochSecond,
  updatedAt: Long = ZonedDateTime.now.toEpochSecond
)
