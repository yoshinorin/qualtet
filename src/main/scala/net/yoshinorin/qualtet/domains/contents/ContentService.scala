package net.yoshinorin.qualtet.domains.contents

import cats.data.{ContT, EitherT}
import cats.Monad
import cats.implicits.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}
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

class ContentService[G[_]: Monad, F[_]: Monad](
  contentRepositoryAdapter: ContentRepositoryAdapter[G],
  tagRepositoryAdapter: TagRepositoryAdapter[G],
  tagService: TagService[G, F],
  contentTaggingRepositoryAdapter: ContentTaggingRepositoryAdapter[G],
  robotsRepositoryAdapter: RobotsRepositoryAdapter[G],
  externalResourceRepositoryAdapter: ExternalResourceRepositoryAdapter[G],
  authorService: AuthorService[G, F],
  contentTypeService: ContentTypeService[G, F],
  seriesRepositoryAdapter: SeriesRepositoryAdapter[G],
  seriesService: SeriesService[G, F],
  contentSerializingRepositoryAdapter: ContentSerializingRepositoryAdapter[G]
)(using executer: Executer[G, F], loggerFactory: Log4CatsLoggerFactory[F]) {

  private given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  def createOrUpdate(authorName: AuthorName, request: ContentRequestModel): F[Either[DomainError, ContentResponseModel]] = {
    val authorAndContentType = for {
      contentTypeName <- EitherT.fromEither[F](ContentTypeName(request.contentType))
      author <- EitherT(
        authorService
          .findByName(authorName)
          .errorIfNone(InvalidAuthor(detail = s"user not found: ${authorName}"))
          .logLeftF(Warn)
      )
      contentType <- EitherT(
        contentTypeService
          .findByName(contentTypeName)
          .errorIfNone(InvalidContentType(detail = s"content-type not found: ${request.contentType}"))
          .logLeftF(Warn)
      )
    } yield (author, contentType)

    val created: EitherT[F, DomainError, ContentResponseModel] = for {
      (author, contentType) <- authorAndContentType
      maybeCurrentContent <- EitherT.liftF(this.findByPath(request.path))
      contentId = maybeCurrentContent.map(_.id).getOrElse(ContentId.apply())
      maybeTags <- EitherT.liftF(tagService.getTags(Some(request.tags)))
      contentTaggings <- EitherT.liftF(maybeTags match {
        case None => Monad[F].pure(List())
        case Some(x) => Monad[F].pure(x.map(t => ContentTagging(contentId, t.id)))
      })
      contentSerilizing <- request.series match {
        case None => EitherT.rightT[F, DomainError](None)
        case Some(seriesName) =>
          EitherT(seriesService.findByName(seriesName).flatMap {
            case Some(s) => Monad[F].pure(Right(Some(ContentSerializing(s.id, contentId))))
            case None => Left(InvalidSeries(detail = s"series not found: ${seriesName}")).logLeft[F](Warn)
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
  ): F[Either[DomainError, Content]] = {

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
        case _ => Monad[G].pure(())
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
      c <- this
        .findByPath(data.path)
        .errorIfNone(UnexpectedException("content not found"))
        .logLeftF(Error) // NOTE: 404 is better?
    } yield c
  }

  /**
   * delete a content by id
   *
   * @param id Instance of ContentId
   */
  def delete(id: ContentId): F[Either[DomainError, Unit]] = {

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
        Left(ContentNotFound(detail = s"content not found: ${id}")).logLeft[F](Warn)
    }
  }

  /**
   * Find a content by path
   *
   * @param path a content path
   * @return ResponseContent instance
   */
  def findByPath(path: ContentPath): F[Option[Content]] = {
    executer.transact(contentRepositoryAdapter.findByPath(path))
  }

  /**
   * Find a content by path
   *
   * @param path a content path
   * @return ResponseContent instance
   */
  def findByPathWithMeta(path: ContentPath): F[Either[DomainError, Option[ContentDetailResponseModel]]] = {
    this.findBy(path)(contentRepositoryAdapter.findByPathWithMeta)
  }

  /**
   * Find a content by id
   *
   * @param id ContentId
   * @return ResponseContent instance
   */
  def findById(id: ContentId): F[Option[Content]] = {
    executer.transact(contentRepositoryAdapter.findById(id))
  }

  def findAdjacent(id: ContentId): F[Either[DomainError, Option[AdjacentContentResponseModel]]] = {
    executer.transact(contentRepositoryAdapter.findAdjacent(id)).map {
      case None => Right(None)
      case Some((previous, next)) => Right(Some(AdjacentContentResponseModel(previous, next)))
    }
  }

  def findBy[A](
    data: A
  )(
    f: A => ContT[G, Either[DomainError, Option[ContentWithMeta]], Either[DomainError, Option[ContentWithMeta]]]
  ): F[Either[DomainError, Option[ContentDetailResponseModel]]] = {
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
