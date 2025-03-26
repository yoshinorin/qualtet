package net.yoshinorin.qualtet.domains.contentTaggings

import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.domains.contents.ContentRequestModel
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieExecuter
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.contentTaggings.ContentTaggingRepositoryASpec
class ContentTaggingRepositoryASpec extends AnyWordSpec with BeforeAndAfterAll {

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
            tags = List(s"ContentTaggingRepositoryAS${i}.1", s"ContentTaggingRepositoryAS${i}.2", s"ContentTaggingRepositoryAS${i}.3"),
            externalResources = List()
          )
        )
    }
    requestContents.unsafeCreateConternt()
  }

  "ContentTaggingService" should {

    "delete bulky" in {
      // TODO: clean up
      val contents1 = contentService.findByPath(Path("/test/ContentTaggingRepositoryAS-1")).unsafeRunSync().get
      val contents = contentService.findByPathWithMeta(Path("/test/ContentTaggingRepositoryAS-1")).unsafeRunSync().get
      val shouledDeleteContentTaggings = (contents1.id, Seq(contents.tags.head.id, contents.tags.last.id))

      doobieExecuterContext.transact(contentTaggingRepositoryAdapter.bulkDelete(shouledDeleteContentTaggings)).unsafeRunSync()
      val result = contentService.findByPathWithMeta(Path("/test/ContentTaggingRepositoryAS-1")).unsafeRunSync().get

      assert(result.tags.size === 1)
      assert(result.tags.head === contents.tags(1))
    }

    "not be delete any tag" in {
      val beforeDelete = contentService.findByPath(Path("/test/ContentTaggingRepositoryAS-2")).unsafeRunSync().get
      doobieExecuterContext.transact(contentTaggingRepositoryAdapter.bulkDelete(beforeDelete.id, Seq())).unsafeRunSync()
      val afterDelete = contentService.findByPathWithMeta(Path("/test/ContentTaggingRepositoryAS-2")).unsafeRunSync().get

      assert(afterDelete.tags.size === 3)
    }
  }

}
