package net.yoshinorin.qualtet.domains.models.contentTypes

import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeSpec
class ContentTypeSpec extends AnyWordSpec {

  "ContentTypeId" should {
    "create instance with specific id" in {
      assert(ContentTypeId("5214b4e2-485e-41b2-9e1f-996fc75bd879").value == "5214b4e2-485e-41b2-9e1f-996fc75bd879")
    }

    "can not create instance" in {
      // TODO: declare exception
      assertThrows[Exception] {
        ContentTypeId("not-a-UUID")
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
