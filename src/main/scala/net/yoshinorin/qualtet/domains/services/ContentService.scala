package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.Fail.{InternalServerError, NotFound}
import net.yoshinorin.qualtet.domains.models.authors.{AuthorName, ResponseAuthor}
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentType
import net.yoshinorin.qualtet.domains.models.contents.{Content, ContentRepository, Path, RequestContent}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import net.yoshinorin.qualtet.utils.Markdown.renderHtml

class ContentService(
  contentRepository: ContentRepository,
  authorService: AuthorService,
  contentTypeService: ContentTypeService
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
      createdContent <- this.create(
        Content(
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
        )
      )
    } yield createdContent
  }

  /**
   * create a content
   *
   * @param data Instance of Content
   * @return Instance of created Content with IO
   */
  def create(data: Content): IO[Content] = {

    def content: IO[Content] = this.findByPath(data.path).flatMap {
      case None => IO.raiseError(InternalServerError("content not found")) //NOTE: 404 is better?
      case Some(x) => IO(x)
    }

    for {
      _ <- contentRepository.upsert(data).transact(doobieContext.transactor)
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
   * get all contents
   *
   * @return Instance of Contents with IO
   */
  def getAll: IO[Seq[Content]] = {
    contentRepository.getAll.transact(doobieContext.transactor)
  }

}
