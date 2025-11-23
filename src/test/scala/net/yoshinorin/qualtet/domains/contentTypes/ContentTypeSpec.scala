package net.yoshinorin.qualtet.domains.contentTypes

import net.yoshinorin.qualtet.fixture.unsafe
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.errors.InvalidContentTypeName
import net.yoshinorin.qualtet.fixture.Fixture.contentTypeId
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.contentTypes.ContentTypeSpec
class ContentTypeSpec extends AnyWordSpec {

  "ContentTypeId" should {
    "create instance with specific id" in {
      assert(contentTypeId.value === "01febb1333pd3431q1a1e01fbc")
    }

    "can not create instance" in {
      assertThrows[IllegalArgumentException] {
        ContentTypeId("not-a-ULID")
      }
    }
  }

  "ContentTypeName" should {
    "valid value" in {
      assert(ContentTypeName("123AbcDef_-").unsafe.value === "123abcdef_-")
    }
    "invalid value" in {
      val result1 = ContentTypeName("123AbcDef_-.")
      assert(result1.isLeft)
      assert(result1.left.get.isInstanceOf[InvalidContentTypeName])

      val result2 = ContentTypeName("123AbcDef_-!")
      assert(result2.isLeft)
      assert(result2.left.get.isInstanceOf[InvalidContentTypeName])
    }
  }

  "ContentTypeName.fromTrusted" should {
    "normalize to lowercase" in {
      val name = ContentTypeName.fromTrusted("Article")
      assert(name.value === "article")
    }

    "handle already lowercase input" in {
      val name = ContentTypeName.fromTrusted("article")
      assert(name.value === "article")
    }

    "skip validation for invalid characters" in {
      val name = ContentTypeName.fromTrusted("invalid@type")
      assert(name.value === "invalid@type")
    }
  }

  "ContentType" should {
    "default instance" in {
      val content = ContentType(
        name = ContentTypeName("article").unsafe
      )
      assert(content.id.isInstanceOf[ContentTypeId])
    }
  }

}
