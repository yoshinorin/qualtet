package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.domains.{PathValidator, UlidConvertible, ValueExtender}
import net.yoshinorin.qualtet.domains.authors.{AuthorId, AuthorName}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.errors.InvalidPath
import net.yoshinorin.qualtet.domains.externalResources.ExternalResources
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.tags.Tag

import java.time.ZonedDateTime
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

opaque type ContentId = String
object ContentId extends ValueExtender[ContentId] with UlidConvertible[ContentId] {
  given codecContentId: JsonValueCodec[ContentId] = JsonCodecMaker.make
}

opaque type ContentPath = String
object ContentPath extends ValueExtender[ContentPath] with PathValidator {
  given codecPath: JsonValueCodec[ContentPath] = JsonCodecMaker.make

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
    for {
      _ <- validate(value)
      _ <- if (isReserved(value)) Left(InvalidPath(detail = s"Path contains reserved word: ${value}")) else Right(())
    } yield normalize(value)
  }

  private[domains] def fromTrusted(value: String): ContentPath = normalize(value)
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
  externalResources: List[ExternalResources],
  tags: List[Tag],
  content: String,
  authorName: AuthorName,
  publishedAt: Long = ZonedDateTime.now.toEpochSecond,
  updatedAt: Long = ZonedDateTime.now.toEpochSecond
)
