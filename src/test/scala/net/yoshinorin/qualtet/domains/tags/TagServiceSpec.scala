package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
// import net.yoshinorin.qualtet.domains.contentTaggings.ContentTaggingRepository
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.fixture.Fixture._
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.message.Fail

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.TagServiceSpec
class TagServiceSpec extends AnyWordSpec {

  val requestContents: List[RequestContent] = {
    (0 until 10).toList.map(_.toString()).map(i =>
      RequestContent(
        contentType = "article",
        path = Path(s"/test/tagService-${i}"),
        title = s"this is a tagService title ${i}",
        rawContent = s"this is a tagService raw content ${i}",
        htmlContent = s"this is a tagService html content ${i}",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = Option(List(s"tagService${i}")),
        externalResources = Option(List())
      )
    )
  }

  // NOTE: create content and related data for test
  requestContents.foreach { rc => contentService.createContentFromRequest(AuthorName(author.name.value), rc).unsafeRunSync() }

  "TagService" should {

    "be get all tags" in {
      val result = tagService.getAll.unsafeRunSync().filter(t => t.name.value.contains("tagService"))
      assert(result.size === 10)
    }

    "be findByName" in {
      val result = tagService.findByName(TagName("tagService1")).unsafeRunSync()
      assert(result.size === 1)
      assert(result.get.name.value === "tagService1")
    }

    "be findById" in {
      val result = tagService.findByName(TagName("tagService3")).unsafeRunSync()
      assert(tagService.findById(result.get.id).unsafeRunSync().get.name.value === "tagService3")
    }

    "be getTags" in {
      val result = tagService.getTags(Option(List("tagService1", "tagService2"))).unsafeRunSync()
      assert(result.get(0).name.value === "tagService1")
      assert(result.get(1).name.value === "tagService2")
    }

    "be delete" in {
      val result = (for{
        beforeDeleteTag <- tagService.findByName(TagName("tagService9"))
        _ <- tagService.delete(beforeDeleteTag.get.id)
        afterDeleteTag <- tagService.findById(beforeDeleteTag.get.id)
      } yield (beforeDeleteTag, afterDeleteTag)).unsafeRunSync()

      assert(result._1.get.name.value === "tagService9")
      assert(result._2.isEmpty)
      // TODO: add test
      // val ct = ContentTaggingRepository.findByTagId(result._1.get.id).unsafeRunSync()
      //assert(ct.isEmpty)
    }

    "be throw NotFound exception when delete" in {
      assertThrows[Fail.NotFound] {
        tagService.delete(TagId(generateUlid())).unsafeRunSync()
      }
    }
  }

}
