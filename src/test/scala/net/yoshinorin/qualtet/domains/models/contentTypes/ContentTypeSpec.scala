package net.yoshinorin.qualtet.domains.models.contentTypes

import org.scalatest.wordspec.AnyWordSpec
import java.util.UUID

// testOnly net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeSpec
class ContentTypeSpec extends AnyWordSpec {

  "ContentType" should {
    "default instance" in {
      val content = ContentType(
        name = "article"
      )

      assert(UUID.fromString(content.id).isInstanceOf[UUID])
    }
  }

}
