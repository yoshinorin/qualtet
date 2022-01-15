package net.yoshinorin.qualtet.tasks

import net.yoshinorin.qualtet.domains.models.authors.AuthorName
import net.yoshinorin.qualtet.fixture.Fixture.authorService
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.tasks.CreateAuthorSpec
class CreateAuthorSpec extends AnyWordSpec {

  "CreateAuthor" should {

    "be create author" in {
      CreateAuthor.main(Array("testUser", "tu", "pass"))

      val a = authorService.findByName(AuthorName("testUser")).unsafeRunSync()
      assert(a.get.name.value == "testuser")
    }

    "can not be create author" in {
      CreateAuthor.main(Array("testUser2", "tu"))

      val a = authorService.findByName(AuthorName("testUser2")).unsafeRunSync()
      assert(a.isEmpty)
    }

  }

}
