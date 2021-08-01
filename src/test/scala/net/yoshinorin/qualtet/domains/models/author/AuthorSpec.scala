package net.yoshinorin.qualtet.domains.models.author

import net.yoshinorin.qualtet.domains.models.authors.Author
import org.scalatest.wordspec.AnyWordSpec

import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.util.UUID

// testOnly net.yoshinorin.qualtet.domains.models.author.AuthorSpec
class AuthorSpec extends AnyWordSpec {

  "Author" should {

    "default instance" in {
      val currentUTCDateTime = ZonedDateTime.now(ZoneOffset.UTC)
      val author = Author(name = "JhonDue", displayName = "Jhon")
      val instanceUTCDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(author.createdAt), ZoneOffset.UTC)

      assert(UUID.fromString(author.id).isInstanceOf[UUID])
      assert(author.name == "JhonDue")
      assert(author.displayName == "Jhon")
      assert(instanceUTCDateTime.getYear == currentUTCDateTime.getYear)
      assert(instanceUTCDateTime.getMonth == currentUTCDateTime.getMonth)
      assert(instanceUTCDateTime.getDayOfMonth == currentUTCDateTime.getDayOfMonth)
      assert(instanceUTCDateTime.getHour == currentUTCDateTime.getHour)
    }

    "specific valuse" in {
      val author = Author("cc827369-769d-11eb-a81e-663f66aa018c", "JhonDue", "Jhon", 1625065592)

      assert(author.id == "cc827369-769d-11eb-a81e-663f66aa018c")
      assert(author.name == "JhonDue")
      assert(author.displayName == "Jhon")
      assert(author.createdAt == 1625065592)
    }

  }

}
