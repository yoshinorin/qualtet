package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.fixture.Fixture._
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.TagServiceSpec
class TagServiceSpec extends AnyWordSpec {

  val requestContents: List[RequestContent] = {
    (0 until 10).toList.map(i =>
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
      assert(result.size == 10)
    }

    "be findByName" in {
      val result = tagService.findByName(TagName("tagService1")).unsafeRunSync()
      assert(result.size == 1)
      assert(result.get.name.value == "tagService1")
    }

    "be getTags" in {
      val result = tagService.getTags(Option(List("tagService1", "tagService2"))).unsafeRunSync()
      assert(result.get(0).name.value == "tagService1")
      assert(result.get(1).name.value == "tagService2")
    }

  }

}
