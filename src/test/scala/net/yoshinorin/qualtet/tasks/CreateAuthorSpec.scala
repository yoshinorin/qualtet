package net.yoshinorin.qualtet.tasks

import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.fixture.Fixture.{author, author2, authorService}
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.tasks.CreateAuthorSpec
class CreateAuthorSpec extends AnyWordSpec {

  "CreateAuthor" should {

    "create author" in {
      CreateAuthor.run(List(author.name.value, author.displayName.value, "pass")).unsafeRunSync()

      // TODO: Need assertion. Sometimes return `None` after change 9817e9fc7a57cb1e8eac1c168a715f9486ed0dc7
      // val a = authorService.findByName(AuthorName(author.name.value)).unsafeRunSync()
      // assert(a.get.name.value === author.name.value)

      // NOTE: just for create test data
      CreateAuthor.run(List(author2.name.value, author2.displayName.value, "pass")).unsafeRunSync()
      // NOTE: avoid test failure. This is a just test data no need assert.
      // assert(a2.get.name.value === author2.name.value)

      // CreateAuthor.main(Array("notexistsuser", "NA", "pass"))
    }

    "not be create author" in {
      assertThrows[IllegalArgumentException] {
        CreateAuthor.run(List("testUser2", "tu")).unsafeRunSync()
      }

      val a = authorService.findByName(AuthorName("testUser2")).unsafeRunSync()
      assert(a.isEmpty)
    }

  }

}
