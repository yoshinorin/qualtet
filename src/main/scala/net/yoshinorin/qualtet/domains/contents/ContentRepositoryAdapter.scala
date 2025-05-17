package net.yoshinorin.qualtet.domains.contents

import cats.data.ContT
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contents.ContentPath

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

  private[domains] def findByPathWithMeta(path: ContentPath): ContT[F, Option[ContentWithMeta], Option[ContentWithMeta]] = {
    ContT.apply[F, Option[ContentWithMeta], Option[ContentWithMeta]] { _ =>
      contentRepository.findByPathWithMeta(path).map { content =>
        content match {
          case Some(c) =>
            Some(
              ContentWithMeta(
                id = c.id,
                title = c.title,
                robotsAttributes = c.robotsAttributes,
                externalResourceKindKeys = c.externalResourceKindKeys,
                externalResourceKindValues = c.externalResourceKindValues,
                tagIds = c.tagIds,
                tagNames = c.tagNames,
                tagPaths = c.tagPaths,
                content = c.content,
                authorName = c.authorName,
                publishedAt = c.publishedAt,
                updatedAt = c.updatedAt
              )
            )
          case None => None
        }
      }
    }
  }

}
