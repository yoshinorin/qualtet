package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
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
    // TODO: have to fix
    for {
      maybeAuthor <- authorService.findByName(request.author)
      maybeContentType <- contentTypeService.findByName(request.contentType)
      // TODO: error handling
      createdContent <- this
        .create(
          Content(
            authorId = maybeAuthor.get.id, // TODO: error handling
            contentTypeId = maybeContentType.get.id, // TODO: error handling
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
      _ <- contentRepository.insert(data).transact(doobieContext.transactor)
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
