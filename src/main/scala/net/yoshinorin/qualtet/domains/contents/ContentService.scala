package net.yoshinorin.qualtet.domains.contents

import cats.effect.IO
import cats.Monad
import doobie.util.transactor.Transactor.Aux
import doobie.implicits._
import net.yoshinorin.qualtet.actions.Action._
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.domains.authors.{AuthorName, AuthorService}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.domains.externalResources.{ExternalResource, ExternalResourceKind, ExternalResourceService, ExternalResources}
import net.yoshinorin.qualtet.message.Fail.{InternalServerError, NotFound}
import net.yoshinorin.qualtet.domains.contentTaggings.{ContentTagging, ContentTaggingService}
import net.yoshinorin.qualtet.domains.robots.{Attributes, Robots, RobotsService}
import net.yoshinorin.qualtet.domains.tags.{Tag, TagId, TagName, TagService}
import net.yoshinorin.qualtet.infrastructure.db.DataBaseContext
import net.yoshinorin.qualtet.syntax._
import wvlet.airframe.ulid.ULID

class ContentService[F[_]: Monad](
  contentRepository: ContentRepository[F],
  tagService: TagService[F],
  contentTaggingService: ContentTaggingService[F],
  robotsService: RobotsService[F],
  externalResourceService: ExternalResourceService[F],
  authorService: AuthorService[F],
  contentTypeService: ContentTypeService[F]
)(
  dbContext: DataBaseContext[Aux[IO, Unit]]
) {

  def upsertActions(data: Content): Action[Int] = {
    Continue(contentRepository.upsert(data), Action.done[Int])
  }

  def deleteActions(id: ContentId): Action[Unit] = {
    Continue(contentRepository.delete(id), Action.done[Unit])
  }

  def findByIdActions(id: ContentId): Action[Option[Content]] = {
    Continue(contentRepository.findById(id), Action.done[Option[Content]])
  }

  def findByPathActions(path: Path): Action[Option[Content]] = {
    Continue(contentRepository.findByPath(path), Action.done[Option[Content]])
  }

  def findByPathWithMetaActions(path: Path): Action[Option[ResponseContentDbRow]] = {
    Continue(contentRepository.findByPathWithMeta(path), Action.done[Option[ResponseContentDbRow]])
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
      a <- authorService.findByName(authorName).throwIfNone(NotFound(s"user not found: ${request.contentType}"))
      c <- contentTypeService.findByName(request.contentType).throwIfNone(NotFound(s"content-type not found: ${request.contentType}"))
      maybeCurrentContent <- this.findByPath(request.path)
      contentId = maybeCurrentContent match {
        case None => ContentId(ULID.newULIDString.toLower)
        case Some(x) => x.id
      }
      maybeTags <- tagService.getTags(Some(request.tags))
      maybeContentTagging <- createContentTagging(contentId, maybeTags)
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
    externalResources: List[ExternalResources]
  ): IO[Content] = {

    val maybeExternalResources = externalResources.flatMap(a => a.values.map(v => ExternalResource(data.id, a.kind, v)))

    val queries = for {
      contentUpsert <- upsertActions(data).perform
      robotsUpsert <- robotsService.upsertActions(Robots(data.id, robotsAttributes)).perform
      currentTags <- tagService.findByContentIdActions(data.id).perform
      tagsDiffDelete <- contentTaggingService.bulkDeleteActions(data.id, currentTags.map(_.id).diff(tags.getOrElse(List()).map(t => t.id))).perform
      tagsBulkUpsert <- tagService.bulkUpsertActions(tags).perform
      // TODO: check diff and clean up contentTagging before upsert
      contentTaggingBulkUpsert <- contentTaggingService.bulkUpsertActions(contentTagging).perform
      // TODO: check diff and clean up external_resources before upsert
      externalResourceBulkUpsert <- externalResourceService.bulkUpsertActions(maybeExternalResources).perform
    } yield (contentUpsert, currentTags, tagsDiffDelete, robotsUpsert, tagsBulkUpsert, contentTaggingBulkUpsert, externalResourceBulkUpsert)

    for {
      _ <- queries.transact(dbContext.transactor)
      c <- this.findByPath(data.path).throwIfNone(InternalServerError("content not found")) // NOTE: 404 is better?
    } yield c
  }

  /**
   * delete a content by id
   *
   * @param id Instance of ContentId
   */
  def delete(id: ContentId): IO[Unit] = {

    val queries = for {
      externalResourcesDelete <- externalResourceService.deleteActions(id).perform
      // TODO: Tags should be deleted automatically after delete a content which are not refer from other contents.
      contentTaggingDelete <- contentTaggingService.deleteByContentIdActions(id).perform
      robotsDelete <- robotsService.deleteActions(id).perform
      contentDelete <- deleteActions(id).perform
    } yield (
      externalResourcesDelete,
      contentTaggingDelete,
      robotsDelete,
      contentDelete
    )

    for {
      _ <- this.findById(id).throwIfNone(NotFound(s"content not found: ${id}"))
      _ <- queries.transact(dbContext.transactor)
    } yield ()
  }

  /**
   * Find a content by path
   *
   * @param path a content path
   * @return ResponseContent instance
   */
  def findByPath(path: Path): IO[Option[Content]] = {
    findByPathActions(path).perform.andTransact(dbContext)
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
    findByIdActions(id).perform.andTransact(dbContext)
  }

  def findBy[A](data: A)(f: A => Action[Option[ResponseContentDbRow]]): IO[Option[ResponseContent]] = {

    import net.yoshinorin.qualtet.syntax._

    f(data).perform.andTransact(dbContext).flatMap {
      case None => IO(None)
      case Some(x) =>
        val stripedContent = x.content.stripHtmlTags.replaceAll("\n", "")
        // TODO: Configurable
        val stripedContentLen = if (stripedContent.length > 50) 50 else stripedContent.length

        IO(
          Some(
            ResponseContent(
              title = x.title,
              robotsAttributes = x.robotsAttributes,
              externalResources = (x.externalResourceKindKeys, x.externalResourceKindValues)
                .zipWithGroupBy((x, y) => ExternalResources(ExternalResourceKind(x), y.map(_._2).distinct))
                .getOrElse(List()),
              tags = (x.tagIds, x.tagNames).zip((x, y) => new Tag(new TagId(x), new TagName(y))).map(x => x.distinct).getOrElse(List()),
              description = stripedContent.substring(0, stripedContentLen),
              content = x.content,
              authorName = x.authorName,
              publishedAt = x.publishedAt,
              updatedAt = x.updatedAt
            )
          )
        )
    }
  }
}
