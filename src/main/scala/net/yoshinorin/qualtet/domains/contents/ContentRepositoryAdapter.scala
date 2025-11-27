package net.yoshinorin.qualtet.domains.contents

import cats.data.ContT
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contents.ContentPath
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.domains.externalResources.{ExternalResourceKind, ExternalResources}
import net.yoshinorin.qualtet.domains.tags.{Tag, TagId, TagName, TagPath}

class ContentRepositoryAdapter[F[_]: Monad](
  contentRepository: ContentRepository[F]
) {

  private[domains] def upsert(data: Content): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { _ =>
      val w = ContentWriteModel(
        id = data.id,
        authorId = data.authorId,
        contentTypeId = data.contentTypeId,
        path = data.path,
        title = data.title,
        rawContent = data.rawContent,
        htmlContent = data.htmlContent,
        publishedAt = data.publishedAt,
        updatedAt = data.updatedAt
      )
      contentRepository.upsert(w)
    }
  }

  private[domains] def delete(id: ContentId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { _ =>
      contentRepository.delete(id)
    }
  }

  private[domains] def findById(id: ContentId): ContT[F, Option[Content], Option[Content]] = {
    ContT.apply[F, Option[Content], Option[Content]] { _ =>
      contentRepository.findById(id).map { content =>
        content match {
          case Some(c) =>
            Some(
              Content(
                id = c.id,
                authorId = c.authorId,
                contentTypeId = c.contentTypeId,
                path = c.path,
                title = c.title,
                rawContent = c.rawContent,
                htmlContent = c.htmlContent,
                publishedAt = c.publishedAt,
                updatedAt = c.updatedAt
              )
            )
          case None => None
        }
      }
    }
  }

  private[domains] def findByPath(path: ContentPath): ContT[F, Option[Content], Option[Content]] = {
    ContT.apply[F, Option[Content], Option[Content]] { _ =>
      contentRepository.findByPath(path).map { content =>
        content match {
          case Some(c) =>
            Some(
              Content(
                id = c.id,
                authorId = c.authorId,
                contentTypeId = c.contentTypeId,
                path = c.path,
                title = c.title,
                rawContent = c.rawContent,
                htmlContent = c.htmlContent,
                publishedAt = c.publishedAt,
                updatedAt = c.updatedAt
              )
            )
          case None => None
        }
      }
    }
  }

  private[domains] def findByPathWithMeta(
    path: ContentPath
  ): ContT[F, Either[DomainError, Option[ContentWithMeta]], Either[DomainError, Option[ContentWithMeta]]] = {
    ContT.apply[F, Either[DomainError, Option[ContentWithMeta]], Either[DomainError, Option[ContentWithMeta]]] { _ =>
      contentRepository.findByPathWithMeta(path).map { content =>
        content match {
          case Some(c) =>
            val result: Either[DomainError, Option[ContentWithMeta]] = for {
              // Parse and build ExternalResources: zip kinds with values, group by kind
              externalResources <- (c.externalResourceKindKeys, c.externalResourceKindValues) match {
                case (Some(keys), Some(values)) =>
                  val kindsList = keys.split(",").toList
                  val valuesList = values.split(",").toList

                  // Parse each kind to ExternalResourceKind (returns Either)
                  kindsList.traverse(ExternalResourceKind(_)).map { parsedKinds =>
                    // Zip kinds with values: List[(ExternalResourceKind, String)]
                    parsedKinds
                      .zip(valuesList)
                      // Group by kind: Map[ExternalResourceKind, List[(ExternalResourceKind, String)]]
                      .groupBy(_._1)
                      // Convert to ExternalResources: List[ExternalResources]
                      .map { case (kind, pairs) =>
                        ExternalResources(kind, pairs.map(_._2).distinct)
                      }
                      .toList
                  }
                case _ => Right(List())
              }

              // Parse and build Tags: zip ids, names, and paths
              tags <- (c.tagIds, c.tagNames, c.tagPaths) match {
                case (Some(ids), Some(names), Some(paths)) =>
                  val idsList = ids.split(",").toList
                  val namesList = names.split(",").toList.map(TagName(_))
                  val pathsList = paths.split(",").toList

                  // Parse each path to TagPath (returns Either)
                  pathsList.traverse(TagPath(_)).map { parsedPaths =>
                    // Zip all three lists: List[(String, TagName, TagPath)]
                    idsList
                      .lazyZip(namesList)
                      .lazyZip(parsedPaths)
                      .toList
                      .map { case (id, name, path) =>
                        Tag(TagId(id), name, path)
                      }
                      .distinct
                  }
                case _ => Right(List())
              }
            } yield Some(
              ContentWithMeta(
                id = c.id,
                title = c.title,
                robotsAttributes = c.robotsAttributes,
                externalResources = externalResources,
                tags = tags,
                content = c.content,
                authorName = c.authorName,
                publishedAt = c.publishedAt,
                updatedAt = c.updatedAt
              )
            )
            result
          case None => Right(None)
        }
      }
    }
  }

  private[domains] def findAdjacent(id: ContentId): ContT[
    F,
    Option[(Option[AdjacentContentModel], Option[AdjacentContentModel])],
    Option[(Option[AdjacentContentModel], Option[AdjacentContentModel])]
  ] = {
    ContT.apply[
      F,
      Option[(Option[AdjacentContentModel], Option[AdjacentContentModel])],
      Option[(Option[AdjacentContentModel], Option[AdjacentContentModel])]
    ] { _ =>
      contentRepository.findAdjacent(id)
    }
  }

}
