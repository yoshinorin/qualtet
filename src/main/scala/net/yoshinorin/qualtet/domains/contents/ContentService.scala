package net.yoshinorin.qualtet.domains.contents

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.Action._
import net.yoshinorin.qualtet.domains.{Action, Continue}
import net.yoshinorin.qualtet.domains.authors.{AuthorName, AuthorService}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.domains.externalResources.{ExternalResource, ExternalResourceKind, ExternalResourceService, ExternalResources}
import net.yoshinorin.qualtet.message.Fail.{InternalServerError, NotFound}
import net.yoshinorin.qualtet.domains.robots.{Attributes, Robots, RobotsService}
import net.yoshinorin.qualtet.domains.tags.{Tag, TagId, TagName, TagService}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import net.yoshinorin.qualtet.syntax._
import wvlet.airframe.ulid.ULID
import java.util.Locale

class ContentService(
  tagService: TagService,
  robotsService: RobotsService,
  externalResourceService: ExternalResourceService,
  authorService: AuthorService,
  contentTypeService: ContentTypeService
)(
  doobieContext: DoobieContext
) {

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
        case None => ContentId(ULID.newULIDString.toLowerCase(Locale.ENGLISH))
        case Some(x) => x.id
      }
      maybeTags <- tagService.getTags(request.tags)
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
    externalResources: Option[List[ExternalResources]]
  ): IO[Content] = {

    def actions(data: Content): Action[Int] = {
      Continue(Upsert(data), Action.buildNext[Int])
    }

    val maybeExternalResources = externalResources match {
      case None => None
      case Some(x) => Option(x.flatMap(a => a.values.map(v => ExternalResource(data.id, a.kind, v))))
    }

    val queries = for {
      contentUpsert <- actions(data).perform
      robotsUpsert <- robotsService.upsertWithoutTaransact(Robots(data.id, robotsAttributes))
      // TODO: check diff and clean up tags before upsert
      tagsBulkUpsert <- tagService.bulkUpsertWithoutTaransact(tags)
      // TODO: check diff and clean up contentTagging before upsert
      contentTaggingBulkUpsert <- ContentTaggingRepository.bulkUpsert(contentTagging)
      // TODO: check diff and clean up external_resources before upsert
      externalResourceBulkUpsert <- externalResourceService.bulkUpsertWithoutTaransact(maybeExternalResources)
    } yield (contentUpsert, robotsUpsert, tagsBulkUpsert, contentTaggingBulkUpsert, externalResourceBulkUpsert)

    for {
      _ <- queries.transact(doobieContext.transactor)
      c <- this.findByPath(data.path).throwIfNone(InternalServerError("content not found")) // NOTE: 404 is better?
    } yield c
  }

  /**
   * delete a content by id
   *
   * @param id Instance of ContentId
   */
  def delete(id: ContentId): IO[Unit] = {

    def actions(id: ContentId): Action[Int] = {
      Continue(Delete(id), Action.buildNext[Int])
    }

    val queries = for {
      externalResourcesDelete <- externalResourceService.deleteWithoutTransact(id)
      // TODO: Tags should be deleted automatically after delete a content which are not refer from other contents.
      contentTaggingDelete <- ContentTaggingRepository.deleteByContentId(id)
      robotsDelete <- robotsService.deleteWithoutTransaction(id)
      contentDelete <- actions(id).perform
    } yield (
      externalResourcesDelete,
      contentTaggingDelete,
      robotsDelete,
      contentDelete
    )

    for {
      _ <- this.findById(id).throwIfNone(NotFound(s"content not found: ${id}"))
      _ <- queries.transact(doobieContext.transactor)
    } yield ()
  }

  /**
   * Find a content by path
   *
   * @param path a content path
   * @return ResponseContent instance
   */
  def findByPath(path: Path): IO[Option[Content]] = {

    def actions(path: Path): Action[Option[Content]] = {
      Continue(FindByPath(path), Action.buildNext[Option[Content]])
    }

    actions(path).perform.andTransact(doobieContext)
  }

  /**
   * Find a content by path
   *
   * @param path a content path
   * @return ResponseContent instance
   */
  def findByPathWithMeta(path: Path): IO[Option[ResponseContent]] = {

    def actions(path: Path): Action[Option[ResponseContentDbRow]] = {
      Continue(FindByPathWithMeta(path), Action.buildNext[Option[ResponseContentDbRow]])
    }

    this.findBy(path)(actions)
  }

  /**
   * Find a content by id
   *
   * @param id ContentId
   * @return ResponseContent instance
   */
  def findById(id: ContentId): IO[Option[Content]] = {

    def actions(id: ContentId): Action[Option[Content]] = {
      Continue(FindById(id), Action.buildNext[Option[Content]])
    }

    actions(id).perform.andTransact(doobieContext)
  }

  def findBy[A](data: A)(f: A => Action[Option[ResponseContentDbRow]]): IO[Option[ResponseContent]] = {

    import net.yoshinorin.qualtet.syntax._

    f(data).perform.andTransact(doobieContext).flatMap {
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
                .zipWithGroupBy((x, y) => ExternalResources(ExternalResourceKind(x), y.map(_._2).distinct)),
              tags = (x.tagIds, x.tagNames).zip((x, y) => new Tag(new TagId(x), new TagName(y))).map(x => x.distinct),
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
