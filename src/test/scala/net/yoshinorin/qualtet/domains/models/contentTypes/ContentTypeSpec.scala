package net.yoshinorin.qualtet.domains.models.contentTypes

import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeSpec
class ContentTypeSpec extends AnyWordSpec {

  "ContentTypeId" should {
    "create instance with specific id" in {
      assert(ContentTypeId("01febb1333pd3431q1a1e00fbt").value == "01febb1333pd3431q1a1e00fbt")
    }

    "can not create instance" in {
      // TODO: declare exception
      assertThrows[Exception] {
        ContentTypeId("not-a-ULID")
      }
    }
  }

  "ContentType" should {
    "default instance" in {
      val content = ContentType(
        name = "article"
      )
      assert(content.id.isInstanceOf[ContentTypeId])
    }
  }

}
