package net.yoshinorin.qualtet.domains.contents

import cats.data.{ContT, EitherT}
import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contents.ContentPath
import net.yoshinorin.qualtet.domains.authors.{AuthorName, AuthorService}
import net.yoshinorin.qualtet.domains.contentSerializing.{ContentSerializing, ContentSerializingRepositoryAdapter}
import net.yoshinorin.qualtet.domains.contentTypes.{ContentType, ContentTypeName, ContentTypeService}
import net.yoshinorin.qualtet.domains.externalResources.{ExternalResource, ExternalResourceDeleteModel, ExternalResourceRepositoryAdapter, ExternalResources}
import net.yoshinorin.qualtet.domains.errors.{ContentNotFound, DomainError, InvalidAuthor, InvalidContentType, InvalidSeries, UnexpectedException}
import net.yoshinorin.qualtet.domains.contentTaggings.{ContentTagging, ContentTaggingRepositoryAdapter}
import net.yoshinorin.qualtet.domains.robots.{Attributes, Robots, RobotsRepositoryAdapter}
import net.yoshinorin.qualtet.domains.tags.{Tag, TagRepositoryAdapter, TagService}
import net.yoshinorin.qualtet.domains.series.{Series, SeriesRepositoryAdapter, SeriesService}
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

  def createOrUpdate(authorName: AuthorName, request: ContentRequestModel): IO[Either[DomainError, ContentResponseModel]] = {
    val authorAndContentType = for {
      contentTypeName <- EitherT.fromEither[IO](ContentTypeName(request.contentType))
      author <- EitherT(authorService.findByName(authorName).errorIfNone(InvalidAuthor(detail = s"user not found: ${authorName}")))
      contentType <- EitherT(
        contentTypeService
          .findByName(contentTypeName)
          .errorIfNone(InvalidContentType(detail = s"content-type not found: ${request.contentType}"))
      )
    } yield (author, contentType)

    val created: EitherT[IO, DomainError, ContentResponseModel] = for {
      (author, contentType) <- authorAndContentType
      maybeCurrentContent <- EitherT.liftF(this.findByPath(request.path))
      contentId = maybeCurrentContent.map(_.id).getOrElse(ContentId.apply())
      maybeTags <- EitherT.liftF(tagService.getTags(Some(request.tags)))
      contentTaggings <- EitherT.liftF(maybeTags match {
        case None => IO(List())
        case Some(x) => IO(x.map(t => ContentTagging(contentId, t.id)))
      })
      contentSerilizing <- request.series match {
        case None => EitherT.rightT[IO, DomainError](None)
        case Some(seriesName) =>
          EitherT(seriesService.findByName(seriesName).flatMap {
            case None => IO.pure(Left(InvalidSeries(detail = s"series not found: ${seriesName}")))
            case Some(s) => IO.pure(Right(Some(ContentSerializing(s.id, contentId))))
          })
      }
      createdContent <- EitherT(
        this.createOrUpdate(
          Content(
            id = contentId,
            authorId = author.id,
            contentTypeId = contentType.id,
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
          contentSerilizing,
          request.externalResources
        )
      )
    } yield ContentResponseModel(
      id = createdContent.id,
      authorId = author.id,
      contentTypeId = contentType.id,
      path = createdContent.path,
      title = createdContent.title,
      rawContent = createdContent.rawContent,
      htmlContent = createdContent.htmlContent,
      publishedAt = createdContent.publishedAt,
      updatedAt = createdContent.updatedAt
    )
    created.value
  }

  private def createOrUpdate(
    data: Content,
    robotsAttributes: Attributes,
    tags: Option[List[Tag]],
    contentTaggings: List[ContentTagging],
    contentSerializing: Option[ContentSerializing],
    externalResources: List[ExternalResources]
  ): IO[Either[DomainError, Content]] = {

    val maybeExternalResources = externalResources.flatMap(a => a.values.map(v => ExternalResource(data.id, a.kind, v)))

    val queries = for {
      contentUpsert <- executer.defer(contentRepositoryAdapter.upsert(data))
      robotsUpsert <- executer.defer(robotsRepositoryAdapter.upsert(Robots(data.id, robotsAttributes)))
      currentTags <- executer.defer(tagRepositoryAdapter.findByContentId(data.id))
      tagsDiffDelete <- executer.defer(contentTaggingRepositoryAdapter.bulkDelete(data.id, currentTags.map(_.id).diff(tags.getOrElse(List()).map(t => t.id))))
      tagsBulkUpsert <- executer.defer(tagRepositoryAdapter.bulkUpsert(tags))
      contentTaggingBulkUpsert <- executer.defer(contentTaggingRepositoryAdapter.bulkUpsert(contentTaggings))
      currentContentSeries <- executer.defer(seriesRepositoryAdapter.findByContentId(data.id))
      contentSerializingDiffDelete <- currentContentSeries match {
        case Some(cc) if contentSerializing.isEmpty => executer.defer(contentSerializingRepositoryAdapter.deleteByContentId(data.id))
        case Some(cc) if contentSerializing.map(_.seriesId) != cc.id =>
          executer.defer(contentSerializingRepositoryAdapter.deleteByContentId(data.id))
        case _ => Monad[F].pure(())
      }
      contentSerializingUpsert <- executer.defer(contentSerializingRepositoryAdapter.upsert(contentSerializing))
      currentExternalResources <- executer.defer(externalResourceRepositoryAdapter.findByContentId(data.id))
      externalResourcesDiffDelete <- executer.defer(
        externalResourceRepositoryAdapter.bulkDelete(
          currentExternalResources.diff(maybeExternalResources).map(e => ExternalResourceDeleteModel(e.contentId, e.kind, e.name)).toList
        )
      )
      externalResourceBulkUpsert <- executer.defer(externalResourceRepositoryAdapter.bulkUpsert(maybeExternalResources))
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
      c <- this.findByPath(data.path).errorIfNone(UnexpectedException("content not found")) // NOTE: 404 is better?
    } yield c
  }

  /**
   * delete a content by id
   *
   * @param id Instance of ContentId
   */
  def delete(id: ContentId): IO[Either[DomainError, Unit]] = {

    val queries = for {
      externalResourcesDelete <- executer.defer(externalResourceRepositoryAdapter.delete(id))
      // TODO: Tags should be deleted automatically after delete a content which are not refer from other contents.
      contentTaggingDelete <- executer.defer(contentTaggingRepositoryAdapter.deleteByContentId(id))
      contentSerializingDelete <- executer.defer(contentSerializingRepositoryAdapter.deleteByContentId(id))
      robotsDelete <- executer.defer(robotsRepositoryAdapter.delete(id))
      contentDelete <- executer.defer(contentRepositoryAdapter.delete(id))
    } yield (
      externalResourcesDelete,
      contentTaggingDelete,
      contentSerializingDelete,
      robotsDelete,
      contentDelete
    )

    this.findById(id).flatMap {
      case Some(_) =>
        executer.transact5[Unit, Unit, Unit, Unit, Unit](queries).map(_ => Right(()))
      case None =>
        IO.pure(Left(ContentNotFound(detail = s"content not found: ${id}")))
    }
  }

  /**
   * Find a content by path
   *
   * @param path a content path
   * @return ResponseContent instance
   */
  def findByPath(path: ContentPath): IO[Option[Content]] = {
    executer.transact(contentRepositoryAdapter.findByPath(path))
  }

  /**
   * Find a content by path
   *
   * @param path a content path
   * @return ResponseContent instance
   */
  def findByPathWithMeta(path: ContentPath): IO[Either[DomainError, Option[ContentDetailResponseModel]]] = {
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

  def findAdjacent(id: ContentId): IO[Either[DomainError, Option[AdjacentContentResponseModel]]] = {
    executer.transact(contentRepositoryAdapter.findAdjacent(id)).map {
      case None => Right(None)
      case Some((previous, next)) => Right(Some(AdjacentContentResponseModel(previous, next)))
    }
  }

  def findBy[A](
    data: A
  )(
    f: A => ContT[F, Either[DomainError, Option[ContentWithMeta]], Either[DomainError, Option[ContentWithMeta]]]
  ): IO[Either[DomainError, Option[ContentDetailResponseModel]]] = {
    executer.transact(f(data)).map {
      case Left(error) => Left(error)
      case Right(maybeContentWithMeta) =>
        maybeContentWithMeta match {
          case None => Right(None)
          case Some(x) =>
            val strippedContent = x.content.stripHtmlTags.replaceAll("\n", "")
            // TODO: Configurable
            val descriptionLength = if (strippedContent.length > 50) 50 else strippedContent.length

            Right(
              Some(
                ContentDetailResponseModel(
                  id = x.id,
                  title = x.title,
                  robotsAttributes = x.robotsAttributes,
                  externalResources = x.externalResources,
                  tags = x.tags,
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
}
