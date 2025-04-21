package net.yoshinorin.qualtet.domains.contentTaggings

import cats.effect.IO
import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.domains.contents.ContentRequestModel
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.tags.{Tag, TagName, TagPath}
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieExecuter
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.contentTaggings.ContentTaggingRepositoryAdapterSpec
class ContentTaggingRepositoryAdapterSpec extends AnyWordSpec with BeforeAndAfterAll {

  given doobieExecuterContext: DoobieExecuter = new DoobieExecuter(fixtureTx)

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    val requestContents: List[ContentRequestModel] = {
      List(1, 2)
        .map(_.toString())
        .map(i =>
          ContentRequestModel(
            contentType = "article",
            path = Path(s"/test/ContentTaggingRepositoryAS-${i}"),
            title = s"this is a ContentTaggingRepositoryAS title ${i}",
            rawContent = s"this is a ContentTaggingRepositoryAS raw content ${i}",
            htmlContent = s"this is a ContentTaggingRepositoryAS html content ${i}",
            robotsAttributes = Attributes("noarchive, noimageindex"),
            tags = List(
              Tag(name = TagName(s"ContentTaggingRepositoryAS${i}.1"), path = TagPath(s"ContentTaggingRepositoryAdapterService-path${i}.1")),
              Tag(name = TagName(s"ContentTaggingRepositoryAS${i}.2"), path = TagPath(s"ContentTaggingRepositoryAdapterService-path${i}.2")),
              Tag(name = TagName(s"ContentTaggingRepositoryAS${i}.3"), path = TagPath(s"ContentTaggingRepositoryAdapterService-path${i}.3"))
            ),
            externalResources = List()
          )
        )
    }
    requestContents.unsafeCreateConternt()
  }

  "ContentTaggingRepositoryAdapter" should {

    "delete bulky" in {
      val path: Path = Path("/test/ContentTaggingRepositoryAS-1")
      (for {
        // find current (before delete) content
        maybeFound <- contentService.findByPathWithMeta(path)
        // delete content tagging
        shouledDeleteContentTaggingsData <- IO(maybeFound.get.id, Seq(maybeFound.get.tags.head.id, maybeFound.get.tags.last.id))
        _ <- doobieExecuterContext.transact(contentTaggingRepositoryAdapter.bulkDelete(shouledDeleteContentTaggingsData))
        // find updated (after delete contentTaggings) content
        maybeContentTaggingsDeleted <- contentService.findByPathWithMeta(path)
      } yield {
        assert(maybeContentTaggingsDeleted.nonEmpty)
        maybeContentTaggingsDeleted.map { contentTaggings =>
          assert(contentTaggings.tags.size === 1)
          assert(contentTaggings.tags.head === maybeFound.get.tags(1))
        }
      }).unsafeRunSync()
    }

    "not be delete any tag" in {
      (for {
        before <- contentService.findByPath(Path("/test/ContentTaggingRepositoryAS-2"))
        _ <- doobieExecuterContext.transact(contentTaggingRepositoryAdapter.bulkDelete(before.get.id, Seq()))
        result <- contentService.findByPathWithMeta(Path("/test/ContentTaggingRepositoryAS-2"))
      } yield {
        assert(result.get.tags.size === 3)
      }).unsafeRunSync()
    }
  }

}
