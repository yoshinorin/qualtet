package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contents.ContentPath
import net.yoshinorin.qualtet.domains.errors.{ContentNotFound, InvalidAuthor, InvalidContentType, InvalidSeries}
import net.yoshinorin.qualtet.domains.series.{Series, SeriesName, SeriesPath, SeriesRequestModel}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.tags.{Tag, TagName, TagPath}
import net.yoshinorin.qualtet.domains.externalResources.ExternalResources
import net.yoshinorin.qualtet.domains.externalResources.ExternalResourceKind
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieExecuter
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.contents.ContentServiceSpec
class ContentServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  given doobieExecuterContext: DoobieExecuter = new DoobieExecuter(fixtureTx)

  override protected def beforeAll(): Unit = {
    (List(
      SeriesRequestModel(
        title = "Content Service Spec Series",
        name = SeriesName("contentservice-series"),
        path = SeriesPath("contentservice-series-path"),
        None
      ),
      SeriesRequestModel(
        title = "Content Service Spec Series2",
        name = SeriesName("contentservice-series2"),
        path = SeriesPath("contentservice-series2-path"),
        None
      ),
      SeriesRequestModel(
        title = "Content Service Spec will be delete",
        name = SeriesName("contentservice-willBeDelete"),
        path = SeriesPath("contentservice-willBeDelete-path"),
        None
      ),
      SeriesRequestModel(
        title = "Content Service Spec will not delete",
        name = SeriesName("contentservice-willNotDelete"),
        path = SeriesPath("contentservice-willNotDelete-path"),
        None
      )
    )).unsafeCreateSeries()
  }

  val requestContent1: ContentRequestModel = ContentRequestModel(
    contentType = "article",
    path = ContentPath("/test/path"),
    title = "this is a title",
    rawContent = "this is a raw content",
    htmlContent = "this is a html content",
    robotsAttributes = Attributes("noarchive, noimageindex"),
    tags = List(Tag(name = TagName("Scala"), path = TagPath("scala-path")), Tag(name = TagName("http4s"), path = TagPath("http4s-path"))),
    externalResources = List(
      ExternalResources(
        ExternalResourceKind("js"),
        values = List("test", "foo", "bar")
      )
    )
  )

  "ContentServiceSpec" should {

    "create content and related data" in {
      (for {
        craeted <- contentService.createOrUpdate(AuthorName(author.name.value), requestContent1)
        maybeFound <- contentService.findByPathWithMeta(craeted.path)
      } yield {
        assert(craeted.id.isInstanceOf[ContentId])
        // TODO: assert `AuthorId` and `ContentTypeId`
        assert(craeted.path.value === requestContent1.path.value)
        assert(craeted.title === requestContent1.title)
        assert(craeted.rawContent === requestContent1.rawContent)

        assert(maybeFound.nonEmpty)
        maybeFound.map { found =>
          val tags = found.tags.map(x => x.name.value)
          assert(tags.contains("Scala"))
          assert(tags.contains("http4s"))

          val attributes = found.robotsAttributes
          assert(attributes.value === requestContent1.robotsAttributes.value)

          val externalResources = found.externalResources.head
          assert(externalResources.kind === requestContent1.externalResources.head.kind)
          assert(externalResources.values.sorted === requestContent1.externalResources.head.values.sorted)
        }
      }).unsafeRunSync()
    }

    "upsert them" in {

      val requestContent: ContentRequestModel = ContentRequestModel(
        contentType = "article",
        path = ContentPath("/test/path/ContentServiceSpec/upsert"),
        title = "this is a title",
        rawContent = "this is a raw content",
        htmlContent = "this is a html content",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = List(Tag(name = TagName("Scala"), path = TagPath("scala-path")), Tag(name = TagName("http4s"), path = TagPath("http4s-path"))),
        series = Some(
          Series(
            title = "Content Service Spec Series",
            name = SeriesName("contentservice-series"),
            path = SeriesPath("contentservice-series-path"),
            description = None
          )
        ),
        externalResources = List(
          ExternalResources(
            ExternalResourceKind("js"),
            values = List("test", "foo", "bar")
          )
        )
      )

      val updateRequestContent = requestContent.copy(
        title = "updated title",
        tags = List(Tag(name = TagName("Scala"), path = TagPath("scala-path")), Tag(name = TagName("Scala3"), path = TagPath("scala3-path"))),
        series = Some(
          Series(
            title = "Content Service Spec Series2",
            name = SeriesName("contentservice-series2"),
            path = SeriesPath("contentservice-series2-path"),
            description = None
          )
        ),
        robotsAttributes = Attributes("noarchive"),
        externalResources = List(
          ExternalResources(
            ExternalResourceKind("js"),
            values = List("foo", "bar", "baz")
          )
        )
      )

      (for {
        created <- contentService.createOrUpdate(AuthorName(author.name.value), requestContent)
        maybeCreatedFound <- contentService.findByPathWithMeta(requestContent.path)

        // first time update
        updated <- contentService.createOrUpdate(AuthorName(author.name.value), updateRequestContent)
        maybeFoundUpdated <- contentService.findByPathWithMeta(requestContent.path)
        updatedSeries <- seriesService.findByContentId(updated.id)

        // second time update (delete related tags)
        _ <- contentService.createOrUpdate(AuthorName(author.name.value), updateRequestContent.copy(tags = List()))
        deletedTags <- contentService.findByPathWithMeta(requestContent.path)

        // third time update (delete related series)
        _ <- contentService.createOrUpdate(AuthorName(author.name.value), updateRequestContent.copy(series = None))
        deletedSeries <- seriesService.findByContentId(updated.id)
      } yield {
        assert(created.id === updated.id)
        assert(maybeCreatedFound.nonEmpty)
        assert(maybeFoundUpdated.nonEmpty)

        for {
          foundCreated <- maybeCreatedFound
          // first time update assetion
          foundUpdated <- maybeFoundUpdated
        } yield {
          // TODO: check authorId, ContentTypeId
          assert(foundUpdated.title === updateRequestContent.title)
          assert(foundCreated.publishedAt === foundUpdated.publishedAt)
          assert(foundUpdated.robotsAttributes === updateRequestContent.robotsAttributes)

          val updatedTagNames = foundUpdated.tags.map(x => x.name.value)
          assert(updatedTagNames.contains("Scala"))
          assert(updatedTagNames.contains("Scala3"))
          assert(!updatedTagNames.contains("http4s"))

          val updatedExternalResources = foundUpdated.externalResources.head
          assert(updatedExternalResources.kind === updateRequestContent.externalResources.head.kind)
          assert(updatedExternalResources.values.sorted === updateRequestContent.externalResources.head.values.sorted)
          assert(!updatedExternalResources.values.contains("test"))
          assert(updatedExternalResources.values.contains("foo"))
          assert(updatedExternalResources.values.contains("bar"))
          assert(updatedExternalResources.values.contains("baz"))
        }

        // second time update assetion
        assert(deletedTags.get.tags.isEmpty)
        assert(updatedSeries.get.path === "/contentservice-series2-path")

        // third time update assertion
        assert(deletedSeries.isEmpty)
      }).unsafeRunSync()
    }

    "create with none meta values" in {

      val requestContentNoMetas: ContentRequestModel = ContentRequestModel(
        contentType = "article",
        path = ContentPath("/test/no-metas"),
        title = "this is a title",
        rawContent = "this is a raw content",
        htmlContent = "this is a html content",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = List(),
        externalResources = List()
      )

      (for {
        created <- contentService.createOrUpdate(AuthorName(author.name.value), requestContentNoMetas)
        maybeFound <- contentService.findByPathWithMeta(created.path)
      } yield {
        assert(maybeFound.nonEmpty)
        maybeFound.map { found =>
          assert(found.externalResources.isEmpty)
          assert(found.tags.isEmpty)
          assert(found.content === "this is a html content")
        }
      }).unsafeRunSync()
    }

    "findById" in {
      val createRequestContent = requestContent1.copy(
        path = ContentPath("ContentServiceSpec-FindById")
      )

      (for {
        createdContent <- contentService.createOrUpdate(AuthorName(author.name.value), createRequestContent)
        maybeContent <- contentService.findById(createdContent.id)
      } yield {
        assert(maybeContent.get.path === createRequestContent.path)
      }).unsafeRunSync()
    }

    "return htmlContent if include its field when request create" in {
      val updatedRequestContent = requestContent1.copy(
        htmlContent = "<h1>this is a html content<h1>"
      )

      (for {
        updated <- contentService.createOrUpdate(AuthorName(author.name.value), updatedRequestContent)
        maybeFound <- contentService.findByPathWithMeta(requestContent1.path)
      } yield {
        assert(maybeFound.get.content === updatedRequestContent.htmlContent)
      }).unsafeRunSync()
    }

    "delete" in {
      // create test data for delete
      val shouldDeleteContent: ContentRequestModel = ContentRequestModel(
        contentType = "article",
        path = ContentPath("/test/willbe/delete"),
        title = "this is a title",
        rawContent = "this is a raw content",
        htmlContent = "this is a html content",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = List(
          Tag(name = TagName("WillBeDelete"), path = TagPath("willbedelete-path")),
          Tag(name = TagName("WillBeDelete2"), path = TagPath("willbedelete2-path"))
        ),
        series = Some(
          Series(
            title = "Content Service Spec willBeDelete",
            name = SeriesName("contentservice-willBeDelete"),
            path = SeriesPath("contentservice-willBeDelete-path"),
            description = None
          )
        ),
        externalResources = List(
          ExternalResources(
            ExternalResourceKind("js"),
            values = List("willBeDelete1", "willBeDelete2")
          )
        )
      )

      val shouldNotDeleteContent: ContentRequestModel = shouldDeleteContent.copy(
        path = ContentPath("/test/willnot/delete"),
        tags = List(
          Tag(name = TagName("WillNotDelete"), path = TagPath("willnotdelete-path")),
          Tag(name = TagName("WillNotDelete2"), path = TagPath("willnotdelete2-path"))
        ),
        series = Some(
          Series(
            title = "Content Service willNotDelete Series",
            name = SeriesName("contentservice-willNotDelete"),
            path = SeriesPath("contentservice-willNotDelete-path"),
            description = None
          )
        ),
        externalResources = List(
          ExternalResources(
            ExternalResourceKind("js"),
            values = List("willNotDelete1", "willNotDelete2")
          )
        )
      )

      (for {
        // create test data
        shouldeDelete <- contentService.createOrUpdate(AuthorName(author.name.value), shouldDeleteContent)
        shouldNotDelete <- contentService.createOrUpdate(AuthorName(author.name.value), shouldNotDeleteContent)

        // delete one content
        _ <- contentService.delete(shouldeDelete.id)
        maybeDeleted <- contentService.findByPath(shouldeDelete.path)
        maybeExists <- contentService.findByPath(shouldNotDelete.path)

        // find series by content
        maybeDeletedSeries <- seriesService.findByContentId(shouldeDelete.id)
        maybeExistsSeries <- seriesService.findByContentId(shouldNotDelete.id)
      } yield {
        assert(maybeDeleted.isEmpty)
        assert(maybeExists.get.path === shouldNotDeleteContent.path)
        assert(maybeDeletedSeries.isEmpty)
        assert(maybeExistsSeries.nonEmpty)
      }).unsafeRunSync()
    }

    "throw Content ContentNotFound Exception when not exists content to delete" in {
      assertThrows[ContentNotFound] {
        contentService.delete(ContentId(generateUlid())).unsafeRunSync()
      }
    }

    "throw Author InvalidAuthor Exception" in {
      assertThrows[InvalidAuthor] {
        contentService.createOrUpdate(AuthorName("not_exists_user"), requestContent1).unsafeRunSync()
      }
    }

    "throw Content-Type InvalidContentType Exception" in {
      assertThrows[InvalidContentType] {
        contentService.createOrUpdate(AuthorName(author.name.value), requestContent1.copy(contentType = "not_exists_content-type")).unsafeRunSync()
      }
    }

    "throw Series UnprocessableEntity Exception" in {
      assertThrows[InvalidSeries] {
        contentService
          .createOrUpdate(
            AuthorName(author.name.value),
            requestContent1.copy(series =
              Some(
                Series(
                  name = SeriesName("not_exists_series_name"),
                  path = SeriesPath("not_exists_series_path"),
                  title = "Not exists series title",
                  description = None
                )
              )
            )
          )
          .unsafeRunSync()
      }
    }
  }

}
