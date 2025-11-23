package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.domains.contents.ContentPath
import net.yoshinorin.qualtet.domains.contents.ContentId

trait ContentRepository[F[_]] {
  def upsert(data: ContentWriteModel): F[Int]
  def findById(id: ContentId): F[Option[ContentReadModel]]
  def findByPath(path: ContentPath): F[Option[ContentReadModel]]
  def findByPathWithMeta(path: ContentPath): F[Option[ContentWithMetaReadModel]]
  def findAdjacent(id: ContentId): F[Option[(Option[AdjacentContentModel], Option[AdjacentContentModel])]]
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
      given contentRead: Read[ContentReadModel] =
        Read[(String, String, String, String, String, String, String, Long, Long)].map {
          case (contentId, authorId, contentTypeId, path, title, rawContent, htmlContent, publishedAt, updatedAt) =>
            ContentReadModel(
              ContentId(contentId),
              AuthorId(authorId),
              ContentTypeId(contentTypeId),
              ContentPath.fromTrusted(path),
              title,
              rawContent,
              htmlContent,
              publishedAt,
              updatedAt
            )
        }

      given contentOrOptionRead: Read[Option[ContentReadModel]] =
        Read[(String, String, String, String, String, String, String, Long, Long)].map {
          case (contentId, authorId, contentTypeId, path, title, rawContent, htmlContent, publishedAt, updatedAt) =>
            Some(
              ContentReadModel(
                ContentId(contentId),
                AuthorId(authorId),
                ContentTypeId(contentTypeId),
                ContentPath.fromTrusted(path),
                title,
                rawContent,
                htmlContent,
                publishedAt,
                updatedAt
              )
            )
        }

      given contentWithMetaRead: Read[ContentWithMetaReadModel] =
        Read[(String, String, String, Option[String], Option[String], Option[String], Option[String], Option[String], String, String, Long, Long)].map {
          case (
                id,
                title,
                robotsAttributes,
                externalResourceKindKeys,
                externalResourceKindValues,
                tagIds,
                tagNames,
                tagPaths,
                content,
                authorName,
                publishedAt,
                updatedAt
              ) =>
            ContentWithMetaReadModel(
              ContentId(id),
              title,
              Attributes.fromTrusted(robotsAttributes),
              externalResourceKindKeys,
              externalResourceKindValues,
              tagIds,
              tagNames,
              tagPaths,
              content,
              AuthorName.fromTrusted(authorName),
              publishedAt,
              updatedAt
            )
        }

      given adjacentContentRead: Read[(Option[AdjacentContentModel], Option[AdjacentContentModel])] =
        Read[(Option[String], Option[String], Option[String], Option[Long], Option[String], Option[String], Option[String], Option[Long])].map {
          case (prevId, prevPath, prevTitle, prevPublished, nextId, nextPath, nextTitle, nextPublished) =>
            val previous = for {
              id <- prevId
              path <- prevPath
              title <- prevTitle
              published <- prevPublished
            } yield AdjacentContentModel(
              ContentId(id),
              ContentPath.fromTrusted(path),
              title,
              published
            )

            val next = for {
              id <- nextId
              path <- nextPath
              title <- nextTitle
              published <- nextPublished
            } yield AdjacentContentModel(
              ContentId(id),
              ContentPath.fromTrusted(path),
              title,
              published
            )

            (previous, next)
        }

      given contentWrite: Write[ContentWriteModel] =
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
      override def upsert(data: ContentWriteModel): ConnectionIO[Int] = {
        ContentQuery.upsert.run(data)
      }
      override def findById(id: ContentId): ConnectionIO[Option[ContentReadModel]] = {
        ContentQuery.findById(id).option
      }
      override def findByPath(path: ContentPath): ConnectionIO[Option[ContentReadModel]] = {
        ContentQuery.findByPath(path).option
      }
      override def findByPathWithMeta(path: ContentPath): ConnectionIO[Option[ContentWithMetaReadModel]] = {
        // NOTE: use `.option` instead of `.query[Option[T]].unique`
        //       https://stackoverflow.com/questions/57873699/sql-null-read-at-column-1-jdbc-type-null-but-mapping-is-to-a-non-option-type
        ContentQuery.findByPathWithMeta(path).option
      }
      override def findAdjacent(id: ContentId): ConnectionIO[Option[(Option[AdjacentContentModel], Option[AdjacentContentModel])]] = {
        ContentQuery.findAdjacent(id).option
      }
      override def delete(id: ContentId): ConnectionIO[Unit] = {
        ContentQuery.delete(id).run.map(_ => ())
      }
    }
  }

}
