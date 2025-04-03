package net.yoshinorin.qualtet.domains.contents

import cats.data.ContT
import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.domains.authors.AuthorService
import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contentSerializing.{ContentSerializing, ContentSerializingRepositoryAdapter}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.domains.externalResources.{
  ExternalResource,
  ExternalResourceDeleteModel,
  ExternalResourceKind,
  ExternalResourceRepositoryAdapter,
  ExternalResources
}
import net.yoshinorin.qualtet.domains.errors.{ContentNotFound, InvalidAuthor, InvalidContentType, InvalidSeries, UnexpectedException}
import net.yoshinorin.qualtet.domains.contentTaggings.{ContentTagging, ContentTaggingRepositoryAdapter}
import net.yoshinorin.qualtet.domains.robots.{Attributes, Robots, RobotsRepositoryAdapter}
import net.yoshinorin.qualtet.domains.tags.{Tag, TagId, TagName, TagRepositoryAdapter, TagService}
import net.yoshinorin.qualtet.domains.series.{Series, SeriesName, SeriesRepositoryAdapter, SeriesService}
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

class ContentService[F[_]: Monad](
  contentRepositoryAdapter: ContentRepositoryAdapter[F],
  tagRepositoryAdapter: TagRepositoryAdapter[F],
  tagService: TagService[F],
  contentTaggingRepositoryAdapter: ContentTaggingRepositoryAdapter[F],
  robotsRepositoryAdapter: RobotsRepositoryAdapter[F],
  externalResourceRepositoryAdapter: ExternalResourceRepositoryAdapter[F],
  authorService: AuthorService[F],
  contentTypeService: ContentTypeService[F],
  seriesRepositoryAdapter: SeriesRepositoryAdapter[F],
  seriesService: SeriesService[F],
  contentSerializingRepositoryAdapter: ContentSerializingRepositoryAdapter[F]
)(using
  executer: Executer[F, IO]
) {

  /**
   * create a content from RequestContent case class
   *
   * @param request RequestContent
   * @return created Content with IO
   */
  def create(authorName: AuthorName, request: ContentRequestModel): IO[ContentResponseModel] = {

    for {
      a <- authorService.findByName(authorName).throwIfNone(InvalidAuthor(detail = s"user not found: ${request.contentType}"))
      c <- contentTypeService.findByName(request.contentType).throwIfNone(InvalidContentType(detail = s"content-type not found: ${request.contentType}"))
      maybeCurrentContent <- this.findByPath(request.path)
      contentId = maybeCurrentContent match {
        case None => ContentId.apply()
        case Some(x) => x.id
      }
      maybeTags <- tagService.getTags(Some(request.tags))
      contentTaggings <- maybeTags match {
        case None => IO(List())
        case Some(x) => IO(x.map(t => ContentTagging(contentId, t.id)))
      }
      maybeContentSerializing <- request.series match {
        case None => IO(None)
        case Some(seriesName) =>
          seriesService.findByName(seriesName).flatMap {
            case None => IO.raiseError(InvalidSeries(detail = s"series not found: ${seriesName}"))
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
        contentTaggings,
        maybeContentSerializing,
        request.externalResources
      )
    } yield ContentResponseModel(
      id = createdContent.id,
      authorId = a.id,
      contentTypeId = c.id,
      path = createdContent.path,
      title = createdContent.title,
      rawContent = createdContent.rawContent,
      htmlContent = createdContent.htmlContent,
      publishedAt = createdContent.publishedAt,
      updatedAt = createdContent.updatedAt
    )
  }

  /**
   * create a content
   *
   * @param data Instance of Content
   * @return Instance of created Content with IO
   */
  private def create(
    data: Content,
    robotsAttributes: Attributes,
    tags: Option[List[Tag]],
    contentTaggings: List[ContentTagging],
    contentSerializing: Option[ContentSerializing],
    externalResources: List[ExternalResources]
  ): IO[Content] = {

    val maybeExternalResources = externalResources.flatMap(a => a.values.map(v => ExternalResource(data.id, a.kind, v)))

    val queries = for {
      contentUpsert <- executer.perform(contentRepositoryAdapter.upsert(data))
      robotsUpsert <- executer.perform(robotsRepositoryAdapter.upsert(Robots(data.id, robotsAttributes)))
      currentTags <- executer.perform(tagRepositoryAdapter.findByContentId(data.id))
      tagsDiffDelete <- executer.perform(contentTaggingRepositoryAdapter.bulkDelete(data.id, currentTags.map(_.id).diff(tags.getOrElse(List()).map(t => t.id))))
      tagsBulkUpsert <- executer.perform(tagRepositoryAdapter.bulkUpsert(tags))
      contentTaggingBulkUpsert <- executer.perform(contentTaggingRepositoryAdapter.bulkUpsert(contentTaggings))
      currentContentSeries <- executer.perform(seriesRepositoryAdapter.findByContentId(data.id))
      contentSerializingDiffDelete <- currentContentSeries match {
        case Some(cc) if contentSerializing.isEmpty => executer.perform(contentSerializingRepositoryAdapter.deleteByContentId(data.id))
        case Some(cc) if contentSerializing.map(_.seriesId) != cc.id =>
          executer.perform(contentSerializingRepositoryAdapter.deleteByContentId(data.id))
        case _ => Monad[F].pure(())
      }
      contentSerializingUpsert <- executer.perform(contentSerializingRepositoryAdapter.upsert(contentSerializing))
      currentExternalResources <- executer.perform(externalResourceRepositoryAdapter.findByContentId(data.id))
      externalResourcesDiffDelete <- executer.perform(
        externalResourceRepositoryAdapter.bulkDelete(
          currentExternalResources.diff(maybeExternalResources).map(e => ExternalResourceDeleteModel(e.contentId, e.kind, e.name)).toList
        )
      )
      externalResourceBulkUpsert <- executer.perform(externalResourceRepositoryAdapter.bulkUpsert(maybeExternalResources))
    } yield (
      contentUpsert,
      currentTags,
      tagsDiffDelete,
      robotsUpsert,
      tagsBulkUpsert,
      contentTaggingBulkUpsert,
      currentContentSeries,
      contentSerializingDiffDelete,
      contentSerializingUpsert,
      externalResourcesDiffDelete,
      externalResourceBulkUpsert
    )

    for {
      _ <- executer.transact11[Int, Seq[Tag], Unit, Int, Int, Int, Option[Series], Unit, Int, Unit, Int](queries)
      c <- this.findByPath(data.path).throwIfNone(UnexpectedException("content not found")) // NOTE: 404 is better?
    } yield c
  }

  /**
   * delete a content by id
   *
   * @param id Instance of ContentId
   */
  def delete(id: ContentId): IO[Unit] = {

    val queries = for {
      externalResourcesDelete <- executer.perform(externalResourceRepositoryAdapter.delete(id))
      // TODO: Tags should be deleted automatically after delete a content which are not refer from other contents.
      contentTaggingDelete <- executer.perform(contentTaggingRepositoryAdapter.deleteByContentId(id))
      contentSerializingDelete <- executer.perform(contentSerializingRepositoryAdapter.deleteByContentId(id))
      robotsDelete <- executer.perform(robotsRepositoryAdapter.delete(id))
      contentDelete <- executer.perform(contentRepositoryAdapter.delete(id))
    } yield (
      externalResourcesDelete,
      contentTaggingDelete,
      contentSerializingDelete,
      robotsDelete,
      contentDelete
    )

    for {
      _ <- this.findById(id).throwIfNone(ContentNotFound(detail = s"content not found: ${id}"))
      _ <- executer.transact5[Unit, Unit, Unit, Unit, Unit](queries)
    } yield ()
  }

  /**
   * Find a content by path
   *
   * @param path a content path
   * @return ResponseContent instance
   */
  def findByPath(path: Path): IO[Option[Content]] = {
    executer.transact(contentRepositoryAdapter.findByPath(path))
  }

  /**
   * Find a content by path
   *
   * @param path a content path
   * @return ResponseContent instance
   */
  def findByPathWithMeta(path: Path): IO[Option[ContentDetailResponseModel]] = {
    this.findBy(path)(contentRepositoryAdapter.findByPathWithMeta)
  }

  /**
   * Find a content by id
   *
   * @param id ContentId
   * @return ResponseContent instance
   */
  def findById(id: ContentId): IO[Option[Content]] = {
    executer.transact(contentRepositoryAdapter.findById(id))
  }

  def findBy[A](data: A)(f: A => ContT[F, Option[ContentWithMeta], Option[ContentWithMeta]]): IO[Option[ContentDetailResponseModel]] = {
    executer.transact(f(data)).flatMap {
      case None => IO(None)
      case Some(x) =>
        val strippedContent = x.content.stripHtmlTags.replaceAll("\n", "")
        // TODO: Configurable
        val descriptionLength = if (strippedContent.length > 50) 50 else strippedContent.length

        IO(
          Some(
            ContentDetailResponseModel(
              id = x.id,
              title = x.title,
              robotsAttributes = x.robotsAttributes,
              externalResources = (x.externalResourceKindKeys, x.externalResourceKindValues)
                .zipWithGroupBy((x, y) => ExternalResources(ExternalResourceKind(x), y.map(_._2).distinct))
                .getOrElse(List()),
              tags = (x.tagIds, x.tagNames).zip((x, y) => new Tag(TagId(x), TagName(y))).map(x => x.distinct).getOrElse(List()),
              description = strippedContent.substring(0, descriptionLength),
              content = x.content,
              length = strippedContent.replaceAll(" ", "").length,
              authorName = x.authorName,
              publishedAt = x.publishedAt,
              updatedAt = x.updatedAt
            )
          )
        )
    }
  }
}
