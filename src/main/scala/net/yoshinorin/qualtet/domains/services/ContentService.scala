package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.Fail.{InternalServerError, NotFound}
import net.yoshinorin.qualtet.domains.models.authors.{AuthorName, ResponseAuthor}
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentType
import net.yoshinorin.qualtet.domains.models.contents.{Content, ContentId, ContentRepository, Path, RequestContent, ResponseContent}
import net.yoshinorin.qualtet.domains.models.externalResources.{ExternalResource, ExternalResourceRepository, ExternalResources}
import net.yoshinorin.qualtet.domains.models.robots.{Attributes, Robots, RobotsRepository}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import net.yoshinorin.qualtet.utils.Markdown.renderHtml
import wvlet.airframe.ulid.ULID

class ContentService(
  contentRepository: ContentRepository,
  robotsRepository: RobotsRepository,
  externalResourceRepository: ExternalResourceRepository,
  authorService: AuthorService,
  contentTypeService: ContentTypeService,
  externalResourceService: ExternalResourceService
)(
  implicit doobieContext: DoobieContext
) {

  /**
   * create a content from RequestContent case class
   *
   * @param request RequestContent
   * @return created Content with IO
   */
  def createContentFromRequest(authorName: AuthorName, request: RequestContent): IO[Content] = {

    def author: IO[ResponseAuthor] = authorService.findByName(authorName).flatMap {
      case None => IO.raiseError(NotFound(s"user not found: ${authorName}"))
      case Some(x) => IO(x)
    }

    def contentType: IO[ContentType] = contentTypeService.findByName(request.contentType).flatMap {
      case None => IO.raiseError(NotFound(s"content-type not found: ${request.contentType}"))
      case Some(x) => IO(x)
    }

    for {
      a <- author
      c <- contentType
      maybeCurrentContent <- this.findByPath(request.path)
      createdContent <- this.create(
        Content(
          id = maybeCurrentContent match {
            case None => ContentId(ULID.newULIDString.toLowerCase)
            case Some(x) => x.id
          },
          authorId = a.id,
          contentTypeId = c.id,
          path = request.path,
          title = request.title,
          rawContent = request.rawContent,
          // TODO: render html with apply
          htmlContent = request.htmlContent match {
            case Some(h) => h
            case None => renderHtml(request.rawContent)
          },
          publishedAt = request.publishedAt,
          updatedAt = request.updatedAt
        ),
        request.robotsAttributes,
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
  def create(data: Content, robotsAttributes: Attributes, externalResources: Option[List[ExternalResources]]): IO[Content] = {

    def content: IO[Content] = this.findByPath(data.path).flatMap {
      case None => IO.raiseError(InternalServerError("content not found")) //NOTE: 404 is better?
      case Some(x) => IO(x)
    }

    val maybeExternalResources = externalResources match {
      case None => None
      case Some(x) => Option(x.flatMap(a => a.values.map(v => ExternalResource(data.id, a.kind, v))))
    }

    val queries = for {
      contentUpsert <- contentRepository.upsert(data)
      robotsUpsert <- robotsRepository.upsert(Robots(data.id, robotsAttributes))
      // TODO: check diff and clean up external_resources before upsert
      externalResourceBulkUpsert <- externalResourceRepository.bulkUpsert(maybeExternalResources)
    } yield (contentUpsert, robotsUpsert, externalResourceBulkUpsert)

    for {
      _ <- queries.transact(doobieContext.transactor)
      c <- content
    } yield c
  }

  /**
   * Find a content by path
   *
   * @param path a content path
   * @return ResponseContent instance
   */
  def findByPath(path: Path): IO[Option[Content]] = {
    contentRepository.findByPath(path).transact(doobieContext.transactor)
  }

  /**
   * Find a content by path
   *
   * @param path a content path
   * @return ResponseContent instance
   */
  def findByPathWithMeta(path: Path): IO[Option[ResponseContent]] = {
    contentRepository.findByPathWithMeta(path).transact(doobieContext.transactor).flatMap {
      case None => IO(None)
      case Some(x) =>
        IO(
          Some(
            ResponseContent(
              title = x.title,
              robotsAttributes = x.robotsAttributes,
              externalResources = externalResourceService.toExternalResources(x.externalResourceKindKeys, x.externalResourceKindValues),
              content = x.content,
              publishedAt = x.publishedAt
            )
          )
        )
    }
  }
}
