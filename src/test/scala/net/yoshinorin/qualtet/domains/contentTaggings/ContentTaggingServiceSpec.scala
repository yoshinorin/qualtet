package net.yoshinorin.qualtet.domains.contentTaggings

import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.DoobieAction._
import net.yoshinorin.qualtet.fixture.Fixture._
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.contentTaggings.ContentTaggingServiceSpec
class ContentTaggingServiceSpec extends AnyWordSpec {

  val requestContents: List[RequestContent] = {
    List(1, 2)
      .map(_.toString())
      .map(i =>
        RequestContent(
          contentType = "article",
          path = Path(s"/test/ContentTaggingServiceSpec-${i}"),
          title = s"this is a ContentTaggingServiceSpec title ${i}",
          rawContent = s"this is a ContentTaggingServiceSpec raw content ${i}",
          htmlContent = s"this is a ContentTaggingServiceSpec html content ${i}",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          tags = List(s"ContentTaggingServiceSpec${i}.1", s"ContentTaggingServiceSpec${i}.2", s"ContentTaggingServiceSpec${i}.3"),
          externalResources = List()
        )
      )
  }

  // NOTE: create content and related data for test
  requestContents.foreach { rc => contentService.createContentFromRequest(AuthorName(author.name.value), rc).unsafeRunSync() }

  "ContentTaggingService" should {

    "be delete bulky" in {
      // TODO: clean up
      val contents1 = contentService.findByPath(Path("/test/ContentTaggingServiceSpec-1")).unsafeRunSync().get
      val contents = contentService.findByPathWithMeta(Path("/test/ContentTaggingServiceSpec-1")).unsafeRunSync().get
      val shouledDeleteContentTaggings = (contents1.id, Seq(contents.tags.head.id, contents.tags.last.id))

      contentTaggingService.bulkDeleteActions(shouledDeleteContentTaggings).perform.andTransact(doobieContext).unsafeRunSync()
      val result = contentService.findByPathWithMeta(Path("/test/ContentTaggingServiceSpec-1")).unsafeRunSync().get

      assert(result.tags.size === 1)
      assert(result.tags.head === contents.tags(1))
    }

    "not be delete any tag" in {
      val beforeDelete = contentService.findByPath(Path("/test/ContentTaggingServiceSpec-2")).unsafeRunSync().get
      contentTaggingService.bulkDeleteActions(beforeDelete.id, Seq()).perform.andTransact(doobieContext).unsafeRunSync()
      val afterDelete = contentService.findByPathWithMeta(Path("/test/ContentTaggingServiceSpec-2")).unsafeRunSync().get

      assert(afterDelete.tags.size === 3)
    }
  }

}
