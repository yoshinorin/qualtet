package net.yoshinorin.qualtet.tasks

import net.yoshinorin.qualtet.domains.models.authors.{Author, AuthorName}
import net.yoshinorin.qualtet.fixture.Fixture.{author, author2, authorId, authorId2, authorService}
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.tasks.CreateAuthorSpec
class CreateAuthorSpec extends AnyWordSpec {

  "CreateAuthor" should {

    "be create author" in {
      CreateAuthor.main(Array(author.name.value, author.displayName.value, author.password.value))

      val a = authorService.findByName(AuthorName(author.name.value)).unsafeRunSync()
      assert(a.get.name.value == author.name.value)

      CreateAuthor.main(Array(author2.name.value, author2.displayName.value, author2.password.value))

      val a2 = authorService.findByName(AuthorName(author2.name.value)).unsafeRunSync()
      assert(a2.get.name.value == author2.name.value)
    }

    "can not be create author" in {
      CreateAuthor.main(Array("testUser2", "tu"))

      val a = authorService.findByName(AuthorName("testUser2")).unsafeRunSync()
      assert(a.isEmpty)
    }

  }

}
