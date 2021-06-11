package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.Fail.NotFound
import net.yoshinorin.qualtet.domains.models.authors.Author
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentType
import net.yoshinorin.qualtet.domains.models.contents.{Content, ContentRepository, RequestContent}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

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
  def createContentFromRequest(request: RequestContent): IO[Content] = {

    def user: IO[Author] = authorService.findByName(request.author).flatMap {
      case None => IO.raiseError(NotFound(s"user not found: ${request.author}"))
      case Some(x) => IO(x)
    }

    def contentType: IO[ContentType] = contentTypeService.findByName(request.contentType).flatMap {
      case None => IO.raiseError(NotFound(s"content-type not found: ${request.contentType}"))
      case Some(x) => IO(x)
    }

    for {
      u <- user
      c <- contentType
      createdContent <- this.create(
        Content(
          authorId = u.id,
          contentTypeId = c.id,
          path = request.path,
          title = request.title,
          rawContent = request.rawContent,
          htmlContent = request.rawContent,
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
    for {
      _ <- contentRepository.upsert(data).transact(doobieContext.transactor)
      c <- contentRepository.findByPath(data.path).transact(doobieContext.transactor)
    } yield c
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
