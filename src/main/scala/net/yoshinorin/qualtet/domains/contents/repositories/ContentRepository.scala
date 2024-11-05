package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.domains.contents.{ContentId, Path}

trait ContentRepository[F[_]] {
  def upsert(data: Content): F[Int]
  def findById(id: ContentId): F[Option[Content]]
  def findByPath(path: Path): F[Option[Content]]
  def findByPathWithMeta(path: Path): F[Option[ContentReadModel]]
  def delete(id: ContentId): F[Unit]
}

object ContentRepository {

  import doobie.{Read, Write}
  import doobie.ConnectionIO
  import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
  import net.yoshinorin.qualtet.domains.authors.{AuthorId, AuthorName}
  import net.yoshinorin.qualtet.domains.robots.Attributes

  given ContentRepository: ContentRepository[ConnectionIO] = {
    new ContentRepository[ConnectionIO] {
      given contentRead: Read[Content] =
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

      given contentWithOptionRead: Read[Option[Content]] =
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

      given contentDbRawRead: Read[ContentReadModel] =
        Read[(String, String, String, Option[String], Option[String], Option[String], Option[String], String, String, Long, Long)].map {
          case (
                id,
                title,
                robotsAttributes,
                externalResourceKindKeys,
                externalResourceKindValues,
                tagIds,
                tagNames,
                content,
                authorName,
                publishedAt,
                updatedAt
              ) =>
            ContentReadModel(
              ContentId(id),
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

      given contentWrite: Write[Content] =
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
      override def findByPathWithMeta(path: Path): ConnectionIO[Option[ContentReadModel]] = {
        // NOTE: use `.option` instead of `.query[Option[T]].unique`
        //       https://stackoverflow.com/questions/57873699/sql-null-read-at-column-1-jdbc-type-null-but-mapping-is-to-a-non-option-type
        ContentQuery.findByPathWithMeta(path).option
      }
      override def delete(id: ContentId): ConnectionIO[Unit] = {
        ContentQuery.delete(id).run.map(_ => ())
      }
    }
  }

}
