package net.yoshinorin.qualtet.domains.services

import net.yoshinorin.qualtet.domains.models.authors.AuthorName
import net.yoshinorin.qualtet.domains.models.contents.ContentId
import net.yoshinorin.qualtet.fixture.Fixture._
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.services.ContentServiceSpec
class ContentServiceSpec extends AnyWordSpec {

  "ContentServiceSpec" should {

    "create content and related data" in {

      val result = contentService.createContentFromRequest(AuthorName("testuser"), requestContent1).unsafeRunSync()
      assert(result.id.isInstanceOf[ContentId])
      // TODO: check authorId, ContentTypeId
      assert(result.path.value == requestContent1.path.value)
      assert(result.title == requestContent1.title)
      assert(result.rawContent == requestContent1.rawContent)

    }

    // TODO: upsert test

    // TODO: tagging test

    // TODO: robots test

    // TODO: externalResource test

  }

}
