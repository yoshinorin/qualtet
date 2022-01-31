package net.yoshinorin.qualtet.domains.services

import net.yoshinorin.qualtet.domains.models.Fail.NotFound
import net.yoshinorin.qualtet.domains.models.authors.AuthorName
import net.yoshinorin.qualtet.domains.models.contents.ContentId
import net.yoshinorin.qualtet.domains.models.robots.Attributes
import net.yoshinorin.qualtet.fixture.Fixture._
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.services.ContentServiceSpec
class ContentServiceSpec extends AnyWordSpec {

  "ContentServiceSpec" should {

    "create content and related data" in {

      val result = contentService.createContentFromRequest(AuthorName("testuser"), requestContent1).unsafeRunSync()
      assert(result.id.isInstanceOf[ContentId])
      // TODO: check authorId, ContentTypeId
      assert(result.path.value == requestContent1.path.value)
      assert(result.title == requestContent1.title)
      assert(result.rawContent == requestContent1.rawContent)

      val createdContent = contentService.findByPathWithMeta(requestContent1.path).unsafeRunSync()
      val createdTagNames = createdContent.get.tags.get.map(x => x.name.value)
      assert(createdTagNames.contains("Scala"))
      assert(createdTagNames.contains("Akka"))

      val createdAttributes = createdContent.get.robotsAttributes
      assert(createdAttributes.value == requestContent1.robotsAttributes.value)

      val createdExternalResources = createdContent.get.externalResources.get.head
      assert(createdExternalResources.kind == requestContent1.externalResources.get.head.kind)
      assert(createdExternalResources.values.sorted == requestContent1.externalResources.get.head.values.sorted)

    }

    "be upsert" in {

      val currentContent = contentService.findByPathWithMeta(requestContent1.path).unsafeRunSync().get
      val updatedRequestContent = requestContent1.copy(
        title = "updated title",
        tags = Option(List("Scala", "Scala3")),
        robotsAttributes = Attributes("noarchive")
      )
      contentService.createContentFromRequest(AuthorName("testuser"), updatedRequestContent).unsafeRunSync()
      val updatedContent = contentService.findByPathWithMeta(requestContent1.path).unsafeRunSync().get

      // TODO: add id to response field
      //assert(currentContent.id == updatedContent.id)
      // TODO: check authorId, ContentTypeId

      assert(updatedContent.title == updatedRequestContent.title)
      assert(currentContent.publishedAt == updatedContent.publishedAt)
      assert(updatedContent.robotsAttributes == updatedRequestContent.robotsAttributes)

      val updatedTagNames = updatedContent.tags.get.map(x => x.name.value)
      assert(updatedTagNames.contains("Scala"))
      assert(updatedTagNames.contains("Scala3"))
      assert(updatedTagNames.contains("Akka"))

      // TODO: check clean up deleted tags & externalResources

    }

    "be create with none meta values" in {
      contentService.createContentFromRequest(AuthorName("testuser"), requestContentNoMetas).unsafeRunSync()
      val createdContent = contentService.findByPathWithMeta(requestContentNoMetas.path).unsafeRunSync().get
      assert(createdContent.externalResources.isEmpty)
      assert(createdContent.tags.isEmpty)
      assert(createdContent.content == "this is a html content")
    }

    "be return htmlContent if include its field when request create" in {
      val updatedRequestContent = requestContent1.copy(
        htmlContent = Option("<h1>this is a html content<h1>")
      )
      contentService.createContentFromRequest(AuthorName("testuser"), updatedRequestContent).unsafeRunSync()
      val updatedContent = contentService.findByPathWithMeta(requestContent1.path).unsafeRunSync().get
      assert(updatedContent.content == updatedRequestContent.htmlContent.get)
    }

    "be throw Author NotFound Exception" in {
      assertThrows[NotFound] {
        contentService.createContentFromRequest(AuthorName("not_exists_user"), requestContent1).unsafeRunSync()
      }
    }

  }

}
