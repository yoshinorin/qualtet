package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.errors.{ContentNotFound, InvalidAuthor, InvalidContentType, InvalidSeries}
import net.yoshinorin.qualtet.domains.series.SeriesName
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.externalResources.ExternalResources
import net.yoshinorin.qualtet.domains.externalResources.ExternalResourceKind

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.ContentServiceSpec
class ContentServiceSpec extends AnyWordSpec {

  val requestContent1: ContentRequestModel = ContentRequestModel(
    contentType = "article",
    path = Path("/test/path"),
    title = "this is a title",
    rawContent = "this is a raw content",
    htmlContent = "this is a html content",
    robotsAttributes = Attributes("noarchive, noimageindex"),
    tags = List("Scala", "http4s"),
    externalResources = List(
      ExternalResources(
        ExternalResourceKind("js"),
        values = List("test", "foo", "bar")
      )
    )
  )

  val requestContentNoMetas: ContentRequestModel = ContentRequestModel(
    contentType = "article",
    path = Path("/test/no-metas"),
    title = "this is a title",
    rawContent = "this is a raw content",
    htmlContent = "this is a html content",
    robotsAttributes = Attributes("noarchive, noimageindex"),
    tags = List(),
    externalResources = List()
  )

  "ContentServiceSpec" should {

    "create content and related data" in {

      val result = contentService.createContentFromRequest(AuthorName(author.name.value), requestContent1).unsafeRunSync()
      assert(result.id.isInstanceOf[ContentId])
      // TODO: check authorId, ContentTypeId
      assert(result.path.value === requestContent1.path.value)
      assert(result.title === requestContent1.title)
      assert(result.rawContent === requestContent1.rawContent)

      val createdContent = contentService.findByPathWithMeta(requestContent1.path).unsafeRunSync()
      val createdTagNames = createdContent.get.tags.map(x => x.name.value)
      assert(createdTagNames.contains("Scala"))
      assert(createdTagNames.contains("http4s"))

      val createdAttributes = createdContent.get.robotsAttributes
      assert(createdAttributes.value === requestContent1.robotsAttributes.value)

      val createdExternalResources = createdContent.get.externalResources.head
      assert(createdExternalResources.kind === requestContent1.externalResources.head.kind)
      assert(createdExternalResources.values.sorted === requestContent1.externalResources.head.values.sorted)

    }

    "upsert" in {

      val requestContent: ContentRequestModel = ContentRequestModel(
        contentType = "article",
        path = Path("/test/path/ContentServiceSpec/upsert"),
        title = "this is a title",
        rawContent = "this is a raw content",
        htmlContent = "this is a html content",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = List("Scala", "http4s"),
        externalResources = List(
          ExternalResources(
            ExternalResourceKind("js"),
            values = List("test", "foo", "bar")
          )
        )
      )

      contentService.createContentFromRequest(AuthorName(author.name.value), requestContent).unsafeRunSync()
      val currentContent = contentService.findByPathWithMeta(requestContent.path).unsafeRunSync().get
      val updatedRequestContent = requestContent.copy(
        title = "updated title",
        tags = List("Scala", "Scala3"),
        robotsAttributes = Attributes("noarchive")
      )
      contentService.createContentFromRequest(AuthorName(author.name.value), updatedRequestContent).unsafeRunSync()
      val updatedContent = contentService.findByPathWithMeta(requestContent.path).unsafeRunSync().get

      // TODO: add id to response field
      // assert(currentContent.id === updatedContent.id)
      // TODO: check authorId, ContentTypeId

      assert(updatedContent.title === updatedRequestContent.title)
      assert(currentContent.publishedAt === updatedContent.publishedAt)
      assert(updatedContent.robotsAttributes === updatedRequestContent.robotsAttributes)

      val updatedTagNames = updatedContent.tags.map(x => x.name.value)
      assert(updatedTagNames.contains("Scala"))
      assert(updatedTagNames.contains("Scala3"))
      assert(!updatedTagNames.contains("http4s"))

      contentService.createContentFromRequest(AuthorName(author.name.value), requestContent.copy(tags = List())).unsafeRunSync()

      for {
        r <- contentService.findByPathWithMeta(requestContent.path)
      } yield {
        assert(r.get.tags.isEmpty)
      }
      // TODO: check clean up externalResources

    }

    "create with none meta values" in {
      contentService.createContentFromRequest(AuthorName(author.name.value), requestContentNoMetas).unsafeRunSync()
      val createdContent = contentService.findByPathWithMeta(requestContentNoMetas.path).unsafeRunSync().get
      assert(createdContent.externalResources.isEmpty)
      assert(createdContent.tags.isEmpty)
      assert(createdContent.content === "this is a html content")
    }

    "find by id" in {
      val createRequestContent = requestContent1.copy(
        path = Path("ContentServiceSpec-FindById")
      )

      val result = (for {
        createdContent <- contentService.createContentFromRequest(AuthorName(author.name.value), createRequestContent)
        maybeContent <- contentService.findById(createdContent.id)
      } yield maybeContent).unsafeRunSync()

      assert(result.get.path === createRequestContent.path)
    }

    "return htmlContent if include its field when request create" in {
      val updatedRequestContent = requestContent1.copy(
        htmlContent = "<h1>this is a html content<h1>"
      )
      contentService.createContentFromRequest(AuthorName(author.name.value), updatedRequestContent).unsafeRunSync()
      val updatedContent = contentService.findByPathWithMeta(requestContent1.path).unsafeRunSync().get
      assert(updatedContent.content === updatedRequestContent.htmlContent)
    }

    "delete" in {
      // create test data for delete
      val willBeDeleteContent: ContentRequestModel = ContentRequestModel(
        contentType = "article",
        path = Path("/test/willbe/delete"),
        title = "this is a title",
        rawContent = "this is a raw content",
        htmlContent = "this is a html content",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = List("WillBeDelete", "WillBeDelete2"),
        externalResources = List(
          ExternalResources(
            ExternalResourceKind("js"),
            values = List("willBeDelete1", "willBeDelete2")
          )
        )
      )

      val willNotDeleteContent: ContentRequestModel = ContentRequestModel(
        contentType = "article",
        path = Path("/test/willnot/delete"),
        title = "this is a title",
        rawContent = "this is a raw content",
        htmlContent = "this is a html content",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = List("WillNotDelete", "WillNotDelete2"),
        externalResources = List(
          ExternalResources(
            ExternalResourceKind("js"),
            values = List("willNotDelete1", "willNotDelete2")
          )
        )
      )

      // Create test data
      val x = (for {
        x <- contentService.createContentFromRequest(AuthorName(author.name.value), willBeDeleteContent)
        _ <- contentService.createContentFromRequest(AuthorName(author.name.value), willNotDeleteContent)
      } yield x).unsafeRunSync()

      val willBeDeleteContentResult = contentService.findByPath(x.path).unsafeRunSync().get
      contentService.delete(willBeDeleteContentResult.id).unsafeRunSync()

      val afterDeleteOps = (for {
        maybeNoneContent <- contentService.findByPath(willBeDeleteContentResult.path)
        // TODO: Tags should be deleted automatically after delete a content which are not refer from other contents.
        // maybeNoneTag1 <- tagService.findByName(TagName(willBeDeleteContent.tags.get(0)))
        // maybeNoneTag2 <- tagService.findByName(TagName(willBeDeleteContent.tags.get(1)))
        anotherContent <- contentService.findByPath(willNotDeleteContent.path)
      } yield (maybeNoneContent, anotherContent)).unsafeRunSync()

      assert(afterDeleteOps._1.isEmpty)
      assert(afterDeleteOps._2.get.path === willNotDeleteContent.path)
      // assert(afterDeleteOps._2.isEmpty)
      // assert(afterDeleteOps._3.isEmpty)
      // assert(afterDeleteOps._4.get.path === willNotDeleteContent.path)
    }

    "throw Content ContentNotFound Exception when not exists content to delete" in {
      assertThrows[ContentNotFound] {
        contentService.delete(ContentId(generateUlid())).unsafeRunSync()
      }
    }

    "throw Author InvalidAuthor Exception" in {
      assertThrows[InvalidAuthor] {
        contentService.createContentFromRequest(AuthorName("not_exists_user"), requestContent1).unsafeRunSync()
      }
    }

    "throw Content-Type InvalidContentType Exception" in {
      assertThrows[InvalidContentType] {
        contentService.createContentFromRequest(AuthorName(author.name.value), requestContent1.copy(contentType = "not_exists_content-type")).unsafeRunSync()
      }
    }

    "throw Series UnprocessableEntity Exception" in {
      assertThrows[InvalidSeries] {
        contentService
          .createContentFromRequest(AuthorName(author.name.value), requestContent1.copy(series = Some(SeriesName("not_exists_series_name"))))
          .unsafeRunSync()
      }
    }
  }

}
