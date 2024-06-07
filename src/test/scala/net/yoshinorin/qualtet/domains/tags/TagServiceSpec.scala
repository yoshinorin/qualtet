package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.domains.contents.Path
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieExecuter
import net.yoshinorin.qualtet.Modules.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import net.yoshinorin.qualtet.message.Fail

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.TagServiceSpec
class TagServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  given dbContext: DoobieExecuter = new DoobieExecuter(config.db)

  val requestContents = makeRequestContents(10, "tagService")

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContents(requestContents)
  }

  "TagService" should {

    "get all tags" in {
      val result = tagService.getAll.unsafeRunSync().filter(t => t.name.value.contains("tagServiceTag"))
      assert(result.size === 10)
    }

    "findByName" in {
      val result = tagService.findByName(TagName("tagServiceTag1")).unsafeRunSync()
      assert(result.size === 1)
      assert(result.get.name.value === "tagServiceTag1")
    }

    "findById" in {
      val result = tagService.findByName(TagName("tagServiceTag3")).unsafeRunSync()
      assert(tagService.findById(result.get.id).unsafeRunSync().get.name.value === "tagServiceTag3")
    }

    "findByContentId" in {
      val r = (for {
        c <- contentService.findByPath(Path("/test/tagService-4"))
        t <- dbContext.transact(tagService.findByContentIdActions(c.get.id))
      } yield t).unsafeRunSync()
      assert(r.head.name === TagName("tagServiceTag4"))
    }

    "getTags" in {
      val result = tagService.getTags(Option(List("tagServiceTag1", "tagServiceTag2"))).unsafeRunSync()
      assert(result.get(0).name.value === "tagServiceTag1")
      assert(result.get(1).name.value === "tagServiceTag2")
    }

    "delete" in {
      val result = (for {
        beforeDeleteTag <- tagService.findByName(TagName("tagServiceTag9"))
        _ <- tagService.delete(beforeDeleteTag.get.id)
        afterDeleteTag <- tagService.findById(beforeDeleteTag.get.id)
      } yield (beforeDeleteTag, afterDeleteTag)).unsafeRunSync()

      assert(result._1.get.name.value === "tagServiceTag9")
      assert(result._2.isEmpty)
      // TODO: add test
      // val ct = ContentTaggingRepository.findByTagId(result._1.get.id).unsafeRunSync()
      // assert(ct.isEmpty)
    }

    "throw NotFound exception when delete" in {
      assertThrows[Fail.NotFound] {
        tagService.delete(TagId(generateUlid())).unsafeRunSync()
      }
    }
  }

}
