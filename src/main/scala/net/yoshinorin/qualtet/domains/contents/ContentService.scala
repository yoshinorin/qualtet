package net.yoshinorin.qualtet.domains.contents

import cats.data.ContT
import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.domains.authors.AuthorService
import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contentSerializing.{ContentSerializing, ContentSerializingService}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeService
import net.yoshinorin.qualtet.domains.externalResources.{
  ExternalResource,
  ExternalResourceDeleteModel,
  ExternalResourceKind,
  ExternalResourceService,
  ExternalResources
}
import net.yoshinorin.qualtet.domains.errors.{ContentNotFound, InvalidAuthor, InvalidContentType, InvalidSeries, UnexpectedException}
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

  def upsertCont(data: Content): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
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

  def deleteCont(id: ContentId): ContT[F, Unit, Unit] = {
    ContT.apply[F, Unit, Unit] { next =>
      contentRepository.delete(id)
    }
  }

  def findByIdCont(id: ContentId): ContT[F, Option[Content], Option[Content]] = {
    ContT.apply[F, Option[Content], Option[Content]] { next =>
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

  def findByPathCont(path: Path): ContT[F, Option[Content], Option[Content]] = {
    ContT.apply[F, Option[Content], Option[Content]] { next =>
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

  def findByPathWithMetaCont(path: Path): ContT[F, Option[ContentWithMeta], Option[ContentWithMeta]] = {
    ContT.apply[F, Option[ContentWithMeta], Option[ContentWithMeta]] { next =>
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

  /**
   * create a content from RequestContent case class
   *
   * @param request RequestContent
   * @return created Content with IO
   */
  def create(authorName: AuthorName, request: ContentRequestModel): IO[ContentResponseModel] = {

    def createContentTagging(contentId: ContentId, tags: Option[List[Tag]]): IO[Option[List[ContentTagging]]] = {
      tags match {
        case None => IO(None)
        case Some(x) => IO(Option(x.map(t => ContentTagging(contentId, t.id))))
      }
    }

    for {
      a <- authorService.findByName(authorName).throwIfNone(InvalidAuthor(detail = s"user not found: ${request.contentType}"))
      c <- contentTypeService.findByName(request.contentType).throwIfNone(InvalidContentType(detail = s"content-type not found: ${request.contentType}"))
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
        maybeContentTagging,
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
    contentTagging: Option[List[ContentTagging]],
    contentSerializing: Option[ContentSerializing],
    externalResources: List[ExternalResources]
  ): IO[Content] = {

    val maybeExternalResources = externalResources.flatMap(a => a.values.map(v => ExternalResource(data.id, a.kind, v)))

    val queries = for {
      contentUpsert <- executer.perform(upsertCont(data))
      robotsUpsert <- executer.perform(robotsService.upsertCont(Robots(data.id, robotsAttributes)))
      currentTags <- executer.perform(tagService.findByContentIdCont(data.id))
      tagsDiffDelete <- executer.perform(contentTaggingService.bulkDeleteCont(data.id, currentTags.map(_.id).diff(tags.getOrElse(List()).map(t => t.id))))
      tagsBulkUpsert <- executer.perform(tagService.bulkUpsertCont(tags))
      contentTaggingBulkUpsert <- executer.perform(contentTaggingService.bulkUpsertCont(contentTagging))
      // TODO: check diff and clean up content_serializing before upsert
      contentSerializingUpsert <- executer.perform(contentSerializingService.upsertCont(contentSerializing))
      currentExternalResources <- executer.perform(externalResourceService.findByContentIdCont(data.id))
      externalResourcesDiffDelete <- executer.perform(
        externalResourceService.bulkDeleteCont(
          currentExternalResources.diff(maybeExternalResources).map(e => ExternalResourceDeleteModel(e.contentId, e.kind, e.name)).toList
        )
      )
      externalResourceBulkUpsert <- executer.perform(externalResourceService.bulkUpsertCont(maybeExternalResources))
    } yield (
      contentUpsert,
      currentTags,
      tagsDiffDelete,
      robotsUpsert,
      tagsBulkUpsert,
      contentTaggingBulkUpsert,
      contentSerializingUpsert,
      externalResourcesDiffDelete,
      externalResourceBulkUpsert
    )

    for {
      _ <- executer.transact9[Int, Seq[Tag], Unit, Int, Int, Int, Int, Unit, Int](queries)
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
      externalResourcesDelete <- executer.perform(externalResourceService.deleteCont(id))
      // TODO: Tags should be deleted automatically after delete a content which are not refer from other contents.
      contentTaggingDelete <- executer.perform(contentTaggingService.deleteByContentIdCont(id))
      robotsDelete <- executer.perform(robotsService.deleteCont(id))
      contentDelete <- executer.perform(deleteCont(id))
      // TODO: delete series
    } yield (
      externalResourcesDelete,
      contentTaggingDelete,
      robotsDelete,
      contentDelete
    )

    for {
      _ <- this.findById(id).throwIfNone(ContentNotFound(detail = s"content not found: ${id}"))
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
    executer.transact(findByPathCont(path))
  }

  /**
   * Find a content by path
   *
   * @param path a content path
   * @return ResponseContent instance
   */
  def findByPathWithMeta(path: Path): IO[Option[ContentDetailResponseModel]] = {
    this.findBy(path)(findByPathWithMetaCont)
  }

  /**
   * Find a content by id
   *
   * @param id ContentId
   * @return ResponseContent instance
   */
  def findById(id: ContentId): IO[Option[Content]] = {
    executer.transact(findByIdCont(id))
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
