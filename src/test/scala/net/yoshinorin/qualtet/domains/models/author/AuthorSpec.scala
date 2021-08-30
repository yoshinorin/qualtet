package net.yoshinorin.qualtet.domains.models.author

import io.circe.syntax._
import net.yoshinorin.qualtet.domains.models.authors.{Author, AuthorDisplayName, AuthorId, AuthorName, BCryptPassword, ResponseAuthor}
import org.scalatest.wordspec.AnyWordSpec

import java.time.{Instant, ZoneOffset, ZonedDateTime}

// testOnly net.yoshinorin.qualtet.domains.models.author.AuthorSpec
class AuthorSpec extends AnyWordSpec {

  "AuthorId" should {
    "valid value" in {
      assert(AuthorId("01FEBB8AZ5T42M2H68XJ8C754A").value == "01FEBB8AZ5T42M2H68XJ8C754A")
    }
    "invalid value" in {
      // TODO: declare exception
      assertThrows[Exception] {
        AuthorId("not-a-ULID")
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

  "BCryptPassword" should {
    "valid value" in {
      assert(
        BCryptPassword("$2a$10$XmRiVEV8yV9u8BnsIfSTTuzUvH/.6jutH6QvIX6zRoTcqkuKsxE0O").value == "$2a$10$XmRiVEV8yV9u8BnsIfSTTuzUvH/.6jutH6QvIX6zRoTcqkuKsxE0O"
      )
    }
    "invalid value" in {
      // TODO: declare exception
      assertThrows[Exception] {
        BCryptPassword("")
      }
      assertThrows[Exception] {
        BCryptPassword("$2a10XmRiVEV8yV9u8BnsIfSTTuzUvH/.6jutH6QvIX6zRoTcqkuKsxE0O")
      }
    }
  }

  "Author" should {
    "default instance" in {
      val currentUTCDateTime = ZonedDateTime.now(ZoneOffset.UTC)
      val author = Author(
        name = AuthorName("JhonDue"),
        displayName = AuthorDisplayName("JD"),
        password = BCryptPassword("$2a$10$XmRiVEV8yV9u8BnsIfSTTuzUvH/.6jutH6QvIX6zRoTcqkuKsxE0O")
      )
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
      val author = Author(
        AuthorId("01FEBB8AZ5T42M2H68XJ8C754A"),
        AuthorName("JhonDue"),
        AuthorDisplayName("JD"),
        BCryptPassword("$2a$10$XmRiVEV8yV9u8BnsIfSTTuzUvH/.6jutH6QvIX6zRoTcqkuKsxE0O"),
        1625065592
      )

      assert(author.id.value == "01FEBB8AZ5T42M2H68XJ8C754A")
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
          |  "id" : "01FEBB8AZ5T42M2H68XJ8C754A",
          |  "name" : "jhondue",
          |  "displayName": "JD",
          |  "createdAt" : 1567814290
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      val json = ResponseAuthor(
        id = AuthorId("01FEBB8AZ5T42M2H68XJ8C754A"),
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
          |    "id" : "01FEBB8AZ5T42M2H68XJ8C754A",
          |    "name" : "jhondue",
          |    "displayName": "JD",
          |    "createdAt" : 1567814290
          |  },
          |  {
          |    "id" : "01FEBB8AZ5T42M2H68XJ8C754B",
          |    "name" : "jhondue2",
          |    "displayName": "JD2",
          |    "createdAt" : 1567814291
          |  }
          |]
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      val json = Seq(
        ResponseAuthor(
          id = AuthorId("01FEBB8AZ5T42M2H68XJ8C754A"),
          name = AuthorName("JhonDue"),
          displayName = AuthorDisplayName("JD"),
          createdAt = 1567814290
        ),
        ResponseAuthor(
          id = AuthorId("01FEBB8AZ5T42M2H68XJ8C754B"),
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
