package net.yoshinorin.qualtet.domains.contents

import wvlet.airframe.ulid.ULID
import java.util.Locale
import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.fixture.Fixture._
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.externalResources.ExternalResources
import net.yoshinorin.qualtet.domains.externalResources.ExternalResourceKind

// testOnly net.yoshinorin.qualtet.domains.ContentServiceSpec
class ContentServiceSpec extends AnyWordSpec {

  "ContentServiceSpec" should {

    "create content and related data" in {

      val result = contentService.createContentFromRequest(AuthorName(author.name.value), requestContent1).unsafeRunSync()
      assert(result.id.isInstanceOf[ContentId])
      // TODO: check authorId, ContentTypeId
      assert(result.path.value === requestContent1.path.value)
      assert(result.title === requestContent1.title)
      assert(result.rawContent === requestContent1.rawContent)

      val createdContent = contentService.findByPathWithMeta(requestContent1.path).unsafeRunSync()
      val createdTagNames = createdContent.get.tags.get.map(x => x.name.value)
      assert(createdTagNames.contains("Scala"))
      assert(createdTagNames.contains("Akka"))

      val createdAttributes = createdContent.get.robotsAttributes
      assert(createdAttributes.value === requestContent1.robotsAttributes.value)

      val createdExternalResources = createdContent.get.externalResources.get.head
      assert(createdExternalResources.kind === requestContent1.externalResources.get.head.kind)
      assert(createdExternalResources.values.sorted === requestContent1.externalResources.get.head.values.sorted)

    }

    "be upsert" in {

      val currentContent = contentService.findByPathWithMeta(requestContent1.path).unsafeRunSync().get
      val updatedRequestContent = requestContent1.copy(
        title = "updated title",
        tags = Option(List("Scala", "Scala3")),
        robotsAttributes = Attributes("noarchive")
      )
      contentService.createContentFromRequest(AuthorName(author.name.value), updatedRequestContent).unsafeRunSync()
      val updatedContent = contentService.findByPathWithMeta(requestContent1.path).unsafeRunSync().get

      // TODO: add id to response field
      //assert(currentContent.id === updatedContent.id)
      // TODO: check authorId, ContentTypeId

      assert(updatedContent.title === updatedRequestContent.title)
      assert(currentContent.publishedAt === updatedContent.publishedAt)
      assert(updatedContent.robotsAttributes === updatedRequestContent.robotsAttributes)

      val updatedTagNames = updatedContent.tags.get.map(x => x.name.value)
      assert(updatedTagNames.contains("Scala"))
      assert(updatedTagNames.contains("Scala3"))
      assert(updatedTagNames.contains("Akka"))

      // TODO: check clean up deleted tags & externalResources

    }

    "be create with none meta values" in {
      contentService.createContentFromRequest(AuthorName(author.name.value), requestContentNoMetas).unsafeRunSync()
      val createdContent = contentService.findByPathWithMeta(requestContentNoMetas.path).unsafeRunSync().get
      assert(createdContent.externalResources.isEmpty)
      assert(createdContent.tags.isEmpty)
      assert(createdContent.content === "this is a html content")
    }

    "be find by id" in {
      val createRequestContent = requestContent1.copy(
        path = Path("ContentServiceSpec-FindById")
      )

      val result = (for {
        createdContent <- contentService.createContentFromRequest(AuthorName(author.name.value), createRequestContent)
        maybeContent <- contentService.findById(createdContent.id)
      } yield maybeContent).unsafeRunSync()

      assert(result.get.path === createRequestContent.path)
    }

    "be return htmlContent if include its field when request create" in {
      val updatedRequestContent = requestContent1.copy(
        htmlContent = "<h1>this is a html content<h1>"
      )
      contentService.createContentFromRequest(AuthorName(author.name.value), updatedRequestContent).unsafeRunSync()
      val updatedContent = contentService.findByPathWithMeta(requestContent1.path).unsafeRunSync().get
      assert(updatedContent.content === updatedRequestContent.htmlContent)
    }

    "be delete" in {
      // create test data for delete
      val willBeDeleteContent: RequestContent = RequestContent(
        contentType = "article",
        path = Path("/test/willbe/delete"),
        title = "this is a title",
        rawContent = "this is a raw content",
        htmlContent = "this is a html content",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = Option(List("WillBeDelete", "WillBeDelete2")),
        externalResources = Option(
          List(
            ExternalResources(
              ExternalResourceKind("js"),
              values = List("willBeDelete1", "willBeDelete2")
            )
          )
        )
      )

      val willNotDeleteContent: RequestContent = RequestContent(
        contentType = "article",
        path = Path("/test/willnot/delete"),
        title = "this is a title",
        rawContent = "this is a raw content",
        htmlContent = "this is a html content",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = Option(List("WillNotDelete", "WillNotDelete2")),
        externalResources = Option(
          List(
            ExternalResources(
              ExternalResourceKind("js"),
              values = List("willNotDelete1", "willNotDelete2")
            )
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

    "be throw Content NotFound Exception when not exists content to delete" in {
      assertThrows[NotFound] {
        contentService.delete(ContentId(ULID.newULIDString.toLowerCase(Locale.ENGLISH))).unsafeRunSync()
      }
    }

    "be throw Author NotFound Exception" in {
      assertThrows[NotFound] {
        contentService.createContentFromRequest(AuthorName("not_exists_user"), requestContent1).unsafeRunSync()
      }
    }
  }

}
