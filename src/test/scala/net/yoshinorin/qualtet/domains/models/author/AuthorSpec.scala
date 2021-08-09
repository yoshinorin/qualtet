package net.yoshinorin.qualtet.domains.models.author

import io.circe.syntax._
import net.yoshinorin.qualtet.domains.models.authors.{Author, AuthorDisplayName, AuthorId, AuthorName, ResponseAuthor}
import org.scalatest.wordspec.AnyWordSpec

import java.time.{Instant, ZoneOffset, ZonedDateTime}

// testOnly net.yoshinorin.qualtet.domains.models.author.AuthorSpec
class AuthorSpec extends AnyWordSpec {

  "AuthorId" should {
    "valid value" in {
      assert(AuthorId("cc827369-769d-11eb-a81e-663f66aa018c").value == "cc827369-769d-11eb-a81e-663f66aa018c")
    }
    "invalid value" in {
      // TODO: declare exception
      assertThrows[Exception] {
        AuthorId("not-a-uuid")
      }
    }
  }

  "AuthorName" should {
    "valid value" in {
      assert(AuthorName("123AbcDef_-").value == "123abcdef_-")
    }
    "invalid value" in {
      // TODO: declare exception
      assertThrows[Exception] {
        AuthorName("123AbcDef_-.")
      }
      assertThrows[Exception] {
        AuthorName("123AbcDef_-!")
      }
    }
  }

  "AuthorDisplayName" should {
    "valid value" in {
      assert(AuthorDisplayName("123AbcDef_-").value == "123AbcDef_-")
    }
    "invalid value" in {
      // TODO: declare exception
      assertThrows[Exception] {
        AuthorDisplayName("123AbcDef_-.")
      }
      assertThrows[Exception] {
        AuthorDisplayName("123AbcDef_-!")
      }
    }
  }

  "Author" should {
    "default instance" in {
      val currentUTCDateTime = ZonedDateTime.now(ZoneOffset.UTC)
      val author = Author(name = AuthorName("JhonDue"), displayName = AuthorDisplayName("JD"), password = "")
      val instanceUTCDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(author.createdAt), ZoneOffset.UTC)

      assert(author.id.isInstanceOf[AuthorId])
      assert(author.name.value == "jhondue")
      assert(author.displayName.value == "JD")
      assert(instanceUTCDateTime.getYear == currentUTCDateTime.getYear)
      assert(instanceUTCDateTime.getMonth == currentUTCDateTime.getMonth)
      assert(instanceUTCDateTime.getDayOfMonth == currentUTCDateTime.getDayOfMonth)
      assert(instanceUTCDateTime.getHour == currentUTCDateTime.getHour)
    }

    "specific values" in {
      val author = Author(AuthorId("cc827369-769d-11eb-a81e-663f66aa018c"), AuthorName("JhonDue"), AuthorDisplayName("JD"), "", 1625065592)

      assert(author.id.value == "cc827369-769d-11eb-a81e-663f66aa018c")
      assert(author.name.value == "jhondue")
      assert(author.displayName.value == "JD")
      assert(author.createdAt == 1625065592)
    }
  }

  "ResponseAuthor" should {
    "as JSON" in {
      val expectJson =
        """
          |{
          |  "id" : "cc827369-769d-11eb-a81e-663f66aa018c",
          |  "name" : "jhondue",
          |  "displayName": "JD",
          |  "createdAt" : 1567814290
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      val json = ResponseAuthor(
        id = AuthorId("cc827369-769d-11eb-a81e-663f66aa018c"),
        name = AuthorName("JhonDue"),
        displayName = AuthorDisplayName("JD"),
        createdAt = 1567814290
      ).asJson.toString.replaceAll("\n", "").replaceAll(" ", "")

      //NOTE: failed equally compare
      assert(json.contains(expectJson))
    }

    "as JSON Array" in {
      val expectJson =
        """
          |[
          |  {
          |    "id" : "cc827369-769d-11eb-a81e-663f66aa018c",
          |    "name" : "jhondue",
          |    "displayName": "JD",
          |    "createdAt" : 1567814290
          |  },
          |  {
          |    "id" : "cc827369-769d-11eb-a81e-663f66aa018d",
          |    "name" : "jhondue2",
          |    "displayName": "JD2",
          |    "createdAt" : 1567814291
          |  }
          |]
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      val json = Seq(
        ResponseAuthor(
          id = AuthorId("cc827369-769d-11eb-a81e-663f66aa018c"),
          name = AuthorName("JhonDue"),
          displayName = AuthorDisplayName("JD"),
          createdAt = 1567814290
        ),
        ResponseAuthor(
          id = AuthorId("cc827369-769d-11eb-a81e-663f66aa018d"),
          name = AuthorName("JhonDue2"),
          displayName = AuthorDisplayName("JD2"),
          createdAt = 1567814291
        )
      ).asJson.toString.replaceAll("\n", "").replaceAll(" ", "")

      //NOTE: failed equally compare
      assert(json.contains(expectJson))
    }
  }

}
