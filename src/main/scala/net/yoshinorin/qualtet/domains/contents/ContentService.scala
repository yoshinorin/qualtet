package net.yoshinorin.qualtet.domains.contents

import cats.data.ContT
import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.authors.{AuthorName, AuthorService}
import net.yoshinorin.qualtet.domains.contentSerializing.{ContentSerializing, ContentSerializingService}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.domains.externalResources.{ExternalResource, ExternalResourceKind, ExternalResourceService, ExternalResources}
import net.yoshinorin.qualtet.message.Fail.{InternalServerError, NotFound, UnprocessableEntity}
import net.yoshinorin.qualtet.domains.contentTaggings.{ContentTagging, ContentTaggingService}
import net.yoshinorin.qualtet.domains.robots.{Attributes, Robots, RobotsService}
import net.yoshinorin.qualtet.domains.tags.{Tag, TagId, TagName, TagService}
import net.yoshinorin.qualtet.domains.series.{Series, SeriesName, SeriesService}
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

class ContentService[F[_]: Monad](
  contentRepository: ContentRepository[F],
  tagService: TagService[F],
  contentTaggingService: ContentTaggingService[F],
  robotsService: RobotsService[F],
  externalResourceService: ExternalResourceService[F],
  authorService: AuthorService[F],
  contentTypeService: ContentTypeService[F],
  seriesService: SeriesService[F],
  contentSerializingService: ContentSerializingService[F]
)(using
  executer: Executer[F, IO]
) {

  def upsertActions(data: Content): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
      contentRepository.upsert(data)
    }
  }

  def deleteActions(id: ContentId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      contentRepository.delete(id)
    }
  }

  def findByIdActions(id: ContentId): ContT[F, Option[Content], Option[Content]] = {
    ContT.apply[F, Option[Content], Option[Content]] { next =>
      contentRepository.findById(id)
    }
  }

  def findByPathActions(path: Path): ContT[F, Option[Content], Option[Content]] = {
    ContT.apply[F, Option[Content], Option[Content]] { next =>
      contentRepository.findByPath(path)
    }
  }

  def findByPathWithMetaActions(path: Path): ContT[F, Option[ReadContentDbRow], Option[ReadContentDbRow]] = {
    ContT.apply[F, Option[ReadContentDbRow], Option[ReadContentDbRow]] { next =>
      contentRepository.findByPathWithMeta(path)
    }
  }

  /**
   * create a content from RequestContent case class
   *
   * @param request RequestContent
   * @return created Content with IO
   */
  def createContentFromRequest(authorName: AuthorName, request: RequestContent): IO[Content] = {

    def createContentTagging(contentId: ContentId, tags: Option[List[Tag]]): IO[Option[List[ContentTagging]]] = {
      tags match {
        case None => IO(None)
        case Some(x) => IO(Option(x.map(t => ContentTagging(contentId, t.id))))
      }
    }

    for {
      a <- authorService.findByName(authorName).throwIfNone(UnprocessableEntity(detail = s"user not found: ${request.contentType}"))
      c <- contentTypeService.findByName(request.contentType).throwIfNone(UnprocessableEntity(detail = s"content-type not found: ${request.contentType}"))
      maybeCurrentContent <- this.findByPath(request.path)
      contentId = maybeCurrentContent match {
        case None => ContentId.apply()
        case Some(x) => x.id
      }
      maybeTags <- tagService.getTags(Some(request.tags))
      maybeContentTagging <- createContentTagging(contentId, maybeTags)
      maybeContentSerializing <- request.series match {
        case None => IO(None)
        case Some(seriesName) =>
          seriesService.findByName(seriesName).flatMap {
            case None => IO.raiseError(UnprocessableEntity(detail = s"series not found: ${seriesName}"))
            case Some(s) => IO(Option(ContentSerializing(s.id, contentId)))
          }
      }
      createdContent <- this.create(
        Content(
          id = contentId,
          authorId = a.id,
          contentTypeId = c.id,
          path = request.path,
          title = request.title,
          rawContent = request.rawContent,
          htmlContent = request.htmlContent,
          publishedAt = request.publishedAt,
          updatedAt = request.updatedAt
        ),
        request.robotsAttributes,
        maybeTags,
        maybeContentTagging,
        maybeContentSerializing,
        request.externalResources
      )
    } yield createdContent
  }

  /**
   * create a content
   *
   * @param data Instance of Content
   * @return Instance of created Content with IO
   */
  def create(
    data: Content,
    robotsAttributes: Attributes,
    tags: Option[List[Tag]],
    contentTagging: Option[List[ContentTagging]],
    contentSerializing: Option[ContentSerializing],
    externalResources: List[ExternalResources]
  ): IO[Content] = {

    val maybeExternalResources = externalResources.flatMap(a => a.values.map(v => ExternalResource(data.id, a.kind, v)))

    val queries = for {
      contentUpsert <- executer.perform(upsertActions(data))
      robotsUpsert <- executer.perform(robotsService.upsertActions(Robots(data.id, robotsAttributes)))
      currentTags <- executer.perform(tagService.findByContentIdActions(data.id))
      tagsDiffDelete <- executer.perform(contentTaggingService.bulkDeleteActions(data.id, currentTags.map(_.id).diff(tags.getOrElse(List()).map(t => t.id))))
      tagsBulkUpsert <- executer.perform(tagService.bulkUpsertActions(tags))
      // TODO: check diff and clean up contentTagging before upsert
      contentTaggingBulkUpsert <- executer.perform(contentTaggingService.bulkUpsertActions(contentTagging))
      contentSerializingUpsert <- executer.perform(contentSerializingService.upsertActions(contentSerializing))
      // TODO: check diff and clean up external_resources before upsert
      externalResourceBulkUpsert <- executer.perform(externalResourceService.bulkUpsertActions(maybeExternalResources))
    } yield (
      contentUpsert,
      currentTags,
      tagsDiffDelete,
      robotsUpsert,
      tagsBulkUpsert,
      contentTaggingBulkUpsert,
      contentSerializingUpsert,
      externalResourceBulkUpsert
    )

    for {
      _ <- executer.transact8[Int, Seq[Tag], Unit, Int, Int, Int, Int, Int](queries)
      c <- this.findByPath(data.path).throwIfNone(InternalServerError("content not found")) // NOTE: 404 is better?
      // TODO: Should return `ResponseContent` instead of `Content`.
    } yield c
  }

  /**
   * delete a content by id
   *
   * @param id Instance of ContentId
   */
  def delete(id: ContentId): IO[Unit] = {

    val queries = for {
      externalResourcesDelete <- executer.perform(externalResourceService.deleteActions(id))
      // TODO: Tags should be deleted automatically after delete a content which are not refer from other contents.
      contentTaggingDelete <- executer.perform(contentTaggingService.deleteByContentIdActions(id))
      robotsDelete <- executer.perform(robotsService.deleteActions(id))
      contentDelete <- executer.perform(deleteActions(id))
    } yield (
      externalResourcesDelete,
      contentTaggingDelete,
      robotsDelete,
      contentDelete
    )

    for {
      _ <- this.findById(id).throwIfNone(NotFound(detail = s"content not found: ${id}"))
      _ <- executer.transact4[Unit, Unit, Unit, Unit](queries)
    } yield ()
  }

  /**
   * Find a content by path
   *
   * @param path a content path
   * @return ResponseContent instance
   */
  def findByPath(path: Path): IO[Option[Content]] = {
    executer.transact(findByPathActions(path))
  }

  /**
   * Find a content by path
   *
   * @param path a content path
   * @return ResponseContent instance
   */
  def findByPathWithMeta(path: Path): IO[Option[ResponseContent]] = {
    this.findBy(path)(findByPathWithMetaActions)
  }

  /**
   * Find a content by id
   *
   * @param id ContentId
   * @return ResponseContent instance
   */
  def findById(id: ContentId): IO[Option[Content]] = {
    executer.transact(findByIdActions(id))
  }

  def findBy[A](data: A)(f: A => ContT[F, Option[ReadContentDbRow], Option[ReadContentDbRow]]): IO[Option[ResponseContent]] = {
    executer.transact(f(data)).flatMap {
      case None => IO(None)
      case Some(x) =>
        val stripedContent = x.content.stripHtmlTags.replaceAll("\n", "")
        // TODO: Configurable
        val descriptionLength = if (stripedContent.length > 50) 50 else stripedContent.length

        IO(
          Some(
            ResponseContent(
              id = x.id,
              title = x.title,
              robotsAttributes = x.robotsAttributes,
              externalResources = (x.externalResourceKindKeys, x.externalResourceKindValues)
                .zipWithGroupBy((x, y) => ExternalResources(ExternalResourceKind(x), y.map(_._2).distinct))
                .getOrElse(List()),
              tags = (x.tagIds, x.tagNames).zip((x, y) => new Tag(TagId(x), TagName(y))).map(x => x.distinct).getOrElse(List()),
              description = stripedContent.substring(0, descriptionLength),
              content = x.content,
              length = stripedContent.replaceAll(" ", "").length,
              authorName = x.authorName,
              publishedAt = x.publishedAt,
              updatedAt = x.updatedAt
            )
          )
        )
    }
  }
}
