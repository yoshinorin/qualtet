package net.yoshinorin.qualtet.domains.contents

import doobie.{Read, Write}
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.authors.{AuthorId, AuthorName}
import net.yoshinorin.qualtet.domains.robots.Attributes

class ContentRepositoryDoobieInterpreter extends ContentRepository[ConnectionIO] {

  implicit val contentRead: Read[Content] =
    Read[(String, String, String, String, String, String, String, Long, Long)].map {
      case (contentId, authorId, contentTypeId, path, title, rawContent, htmlContent, publishedAt, updatedAt) =>
        Content(
          ContentId(contentId),
          AuthorId(authorId),
          ContentTypeId(contentTypeId),
          Path(path),
          title,
          rawContent,
          htmlContent,
          publishedAt,
          updatedAt
        )
    }

  implicit val contentWithOptionRead: Read[Option[Content]] =
    Read[(String, String, String, String, String, String, String, Long, Long)].map {
      case (contentId, authorId, contentTypeId, path, title, rawContent, htmlContent, publishedAt, updatedAt) =>
        Some(
          Content(
            ContentId(contentId),
            AuthorId(authorId),
            ContentTypeId(contentTypeId),
            Path(path),
            title,
            rawContent,
            htmlContent,
            publishedAt,
            updatedAt
          )
        )
    }

  implicit val contentDbRawRead: Read[ResponseContentDbRow] =
    Read[(String, String, Option[String], Option[String], Option[String], Option[String], String, String, Long, Long)].map {
      case (title, robotsAttributes, externalResourceKindKeys, externalResourceKindValues, tagIds, tagNames, content, authorName, publishedAt, updatedAt) =>
        ResponseContentDbRow(
          title,
          Attributes(robotsAttributes),
          externalResourceKindKeys,
          externalResourceKindValues,
          tagIds,
          tagNames,
          content,
          AuthorName(authorName),
          publishedAt,
          updatedAt
        )
    }

  implicit val contentDbRawWithOptionRead: Read[Option[ResponseContentDbRow]] =
    Read[(String, String, Option[String], Option[String], Option[String], Option[String], String, String, Long, Long)].map {
      case (title, robotsAttributes, externalResourceKindKeys, externalResourceKindValues, tagIds, tagNames, content, authorName, publishedAt, updatedAt) =>
        Some(
          ResponseContentDbRow(
            title,
            Attributes(robotsAttributes),
            externalResourceKindKeys,
            externalResourceKindValues,
            tagIds,
            tagNames,
            content,
            AuthorName(authorName),
            publishedAt,
            updatedAt
          )
        )
    }

  implicit val contentWrite: Write[Content] =
    Write[(String, String, String, String, String, String, String, Long, Long)].contramap(c =>
      (
        c.id.value,
        c.authorId.value,
        c.contentTypeId.value,
        c.path.value,
        c.title,
        c.rawContent,
        c.htmlContent,
        c.publishedAt,
        c.updatedAt
      )
    )

  // TODO: do not `run` here
  override def upsert(data: Content): ConnectionIO[Int] = {
    ContentQuery.upsert.run(data)
  }
  override def findById(id: ContentId): ConnectionIO[Option[Content]] = {
    ContentQuery.findById(id).option
  }
  override def findByPath(path: Path): ConnectionIO[Option[Content]] = {
    ContentQuery.findByPath(path).option
  }
  override def findByPathWithMeta(path: Path): ConnectionIO[Option[ResponseContentDbRow]] = {
    // TODO: fix 500 if content is none
    ContentQuery.findByPathWithMeta(path).unique
  }
  override def delete(id: ContentId): ConnectionIO[Unit] = {
    ContentQuery.delete(id).option.map(_ => ())
  }
}
