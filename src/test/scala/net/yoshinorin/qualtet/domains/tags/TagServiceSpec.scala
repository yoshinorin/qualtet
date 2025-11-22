package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.fixture.unsafe
import net.yoshinorin.qualtet.domains.contents.ContentPath
import net.yoshinorin.qualtet.domains.errors.TagNotFound
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieExecuter
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.tags.TagServiceSpec
class TagServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  given doobieExecuterContext: DoobieExecuter = new DoobieExecuter(fixtureTx)

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContentRequestModels(10, "tagService").unsafeCreateConternt()
    tagService.invalidate().unsafeRunSync()
  }

  "TagService" should {

    "get all tags" in {
      (for {
        tags <- tagService.getAll
      } yield {
        val filteredTags = tags.filter(t => t.name.value.contains("tagServiceTag"))
        assert(filteredTags.size === 10)
      }).unsafeRunSync()
    }

    "findByName" in {
      val tagName = TagName("tagServiceTag1")
      (for {
        maybeTag <- tagService.findByName(tagName)
      } yield {
        assert(maybeTag.size === 1)
        assert(maybeTag.get.name.value === tagName.value)
      }).unsafeRunSync()
    }

    "findById" in {
      val tagName = TagName("tagServiceTag3")
      (for {
        maybeTag <- tagService.findByName(tagName)
        found <- tagService.findById(maybeTag.get.id)
      } yield {
        assert(found.get.name.value === tagName.value)
      }).unsafeRunSync()
    }

    "findByContentId" in {
      (for {
        maybeContent <- contentService.findByPath(ContentPath("/test/tagService-4").unsafe)
        maybeTags <- doobieExecuterContext.transact(tagRepositoryAdapter.findByContentId(maybeContent.get.id))
      } yield {
        assert(maybeTags.head.name === TagName("tagServiceTag4"))
      }).unsafeRunSync()
    }

    "getTags" in {
      (for {
        maybeTags <- tagService.getTags(
          Option(
            List(
              Tag(name = TagName("tagServiceTag1"), path = TagPath("tag-service-tag-path1").unsafe),
              Tag(name = TagName("tagServiceTag2"), path = TagPath("tag-service-tag-path2").unsafe)
            )
          )
        )
        maybeNoTags <- tagService.getTags(None)
      } yield {
        assert(maybeTags.get(0).name.value === "tagServiceTag1")
        assert(maybeTags.get(1).name.value === "tagServiceTag2")
        assert(maybeNoTags.isEmpty)
      }).unsafeRunSync()
    }

    "delete" in {
      (for {
        before <- tagService.findByName(TagName("tagServiceTag9"))
        _ <- tagService.delete(before.get.id)
        after <- tagService.findById(before.get.id)
      } yield {
        assert(before.get.name.value === "tagServiceTag9")
        assert(after.isEmpty)
      }).unsafeRunSync()
    }

    "throw TagNotFound exception when delete" in {
      assertThrows[TagNotFound] {
        tagService.delete(TagId(generateUlid())).unsafeRunSync()
      }
    }

    "invalidate" should {
      "callable" in {
        assert(tagService.invalidate().unsafeRunSync() === ())
      }
    }
  }

}
