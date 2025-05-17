package net.yoshinorin.qualtet.domains.contentTypes

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
      assert(ContentTypeName("123AbcDef_-").value === "123abcdef_-")
    }
    "invalid value" in {
      assertThrows[InvalidContentTypeName] {
        ContentTypeName("123AbcDef_-.")
      }
      assertThrows[InvalidContentTypeName] {
        ContentTypeName("123AbcDef_-!")
      }
    }
  }

  "ContentType" should {
    "default instance" in {
      val content = ContentType(
        name = ContentTypeName("article")
      )
      assert(content.id.isInstanceOf[ContentTypeId])
    }
  }

}
