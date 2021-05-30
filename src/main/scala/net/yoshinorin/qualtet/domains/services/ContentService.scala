package net.yoshinorin.qualtet.domains.services

import cats.effect.IO
import doobie.implicits._
import net.yoshinorin.qualtet.application.authors.AuthorFinder
import net.yoshinorin.qualtet.application.contentTypes.ContentTypeFinder
import net.yoshinorin.qualtet.application.contents.{ContentCreator, ContentFinder}
import net.yoshinorin.qualtet.domains.models.contents.{Content, RequestContent}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class ContentService(
  contentFinder: ContentFinder,
  contentCreator: ContentCreator,
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
      _ <- contentCreator.create(data).transact(doobieContext.transactor)
      c <- contentFinder.findByPath(data.path).transact(doobieContext.transactor)
    } yield c
  }

  /**
   * get all contents
   *
   * @return Instance of Contents with IO
   */
  def getAll: IO[Seq[Content]] = {
    contentFinder.getAll.transact(doobieContext.transactor)
  }

}
