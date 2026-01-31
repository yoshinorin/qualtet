package net.yoshinorin.qualtet.domains.contentTaggings

import net.yoshinorin.qualtet.fixture.unsafe
import cats.effect.IO
import cats.implicits.*
import net.yoshinorin.qualtet.domains.contents.{ContentPath, ContentRequestModel}
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
            path = ContentPath(s"/test/ContentTaggingRepositoryAS-${i}").unsafe,
            title = s"this is a ContentTaggingRepositoryAS title ${i}",
            rawContent = s"this is a ContentTaggingRepositoryAS raw content ${i}",
            htmlContent = s"this is a ContentTaggingRepositoryAS html content ${i}",
            robotsAttributes = Attributes("noarchive, noimageindex").unsafe,
            tags = List(
              Tag(name = TagName(s"ContentTaggingRepositoryAS${i}.1"), path = TagPath(s"ContentTaggingRepositoryAdapterService-path${i}.1").unsafe),
              Tag(name = TagName(s"ContentTaggingRepositoryAS${i}.2"), path = TagPath(s"ContentTaggingRepositoryAdapterService-path${i}.2").unsafe),
              Tag(name = TagName(s"ContentTaggingRepositoryAS${i}.3"), path = TagPath(s"ContentTaggingRepositoryAdapterService-path${i}.3").unsafe)
            ),
            externalResources = List()
          ).unsafe
        )
    }
    requestContents.unsafeCreateConternt()
  }

  "ContentTaggingRepositoryAdapter" should {

    "delete bulky" in {
      val path: ContentPath = ContentPath("/test/ContentTaggingRepositoryAS-1").unsafe
      (for {
        // find current (before delete) content
        maybeFound <- contentService.findByPathWithMeta(path).flatMap(_.liftTo[IO])
        // delete content tagging
        shouledDeleteContentTaggingsData <- IO(maybeFound.get.id, Seq(maybeFound.get.tags.head.id, maybeFound.get.tags.last.id))
        _ <- doobieExecuterContext.transact(contentTaggingRepositoryAdapter.bulkDelete(shouledDeleteContentTaggingsData))
        // find updated (after delete contentTaggings) content
        maybeContentTaggingsDeleted <- contentService.findByPathWithMeta(path).flatMap(_.liftTo[IO])
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
        before <- contentService.findByPath(ContentPath("/test/ContentTaggingRepositoryAS-2").unsafe)
        _ <- doobieExecuterContext.transact(contentTaggingRepositoryAdapter.bulkDelete(before.get.id, Seq()))
        result <- contentService.findByPathWithMeta(ContentPath("/test/ContentTaggingRepositoryAS-2").unsafe).flatMap(_.liftTo[IO])
      } yield {
        assert(result.get.tags.size === 3)
      }).unsafeRunSync()
    }
  }

}
