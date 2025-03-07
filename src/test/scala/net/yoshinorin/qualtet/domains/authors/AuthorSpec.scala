package net.yoshinorin.qualtet.domains.authors

import net.yoshinorin.qualtet.domains.authors.{AuthorDisplayName, AuthorId, AuthorName, BCryptPassword}
import net.yoshinorin.qualtet.domains.errors.{InvalidAuthorDisplayName, InvalidAuthorName, Unauthorized}
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec

import java.time.{Instant, ZoneOffset, ZonedDateTime}

// testOnly net.yoshinorin.qualtet.domains.author.AuthorSpec
class AuthorSpec extends AnyWordSpec {

  "AuthorId" should {
    "valid value" in {
      assert(authorId.value === "01febb8az5t42m2h68xj8c754a")
    }
    "invalid value" in {
      assertThrows[IllegalArgumentException] {
        AuthorId("not-a-ULID")
      }
    }
  }

  "AuthorName" should {
    "valid value" in {
      assert(AuthorName("123AbcDef_-").value === "123abcdef_-")
    }
    "invalid value" in {
      assertThrows[InvalidAuthorName] {
        AuthorName("123AbcDef_-.")
      }
      assertThrows[InvalidAuthorName] {
        AuthorName("123AbcDef_-!")
      }
    }
  }

  "AuthorDisplayName" should {
    "valid value" in {
      assert(AuthorDisplayName("123AbcDef_-").value === "123AbcDef_-")
    }
    "invalid value" in {
      assertThrows[InvalidAuthorDisplayName] {
        AuthorDisplayName("123AbcDef_-.")
      }
      assertThrows[InvalidAuthorDisplayName] {
        AuthorDisplayName("123AbcDef_-!")
      }
    }
  }

  "BCryptPassword" should {
    "valid value" in {
      assert(
        validBCryptPassword.value === "$2a$10$XmRiVEV8yV9u8BnsIfSTTuzUvH/.6jutH6QvIX6zRoTcqkuKsxE0O"
      )
    }
    "invalid value" in {
      assertThrows[Unauthorized] {
        BCryptPassword("")
      }
      assertThrows[Unauthorized] {
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
        password = validBCryptPassword
      )
      val instanceUTCDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(author.createdAt), ZoneOffset.UTC)

      assert(author.id.isInstanceOf[AuthorId])
      assert(author.name.value === "jhondue")
      assert(author.displayName.value === "JD")
      assert(instanceUTCDateTime.getYear === currentUTCDateTime.getYear)
      assert(instanceUTCDateTime.getMonth === currentUTCDateTime.getMonth)
      assert(instanceUTCDateTime.getDayOfMonth === currentUTCDateTime.getDayOfMonth)
      assert(instanceUTCDateTime.getHour === currentUTCDateTime.getHour)
    }

    "specific values" in {
      val author = Author(
        authorId,
        AuthorName("JhonDue"),
        AuthorDisplayName("JD"),
        validBCryptPassword,
        1625065592
      )

      assert(author.id.value === "01febb8az5t42m2h68xj8c754a")
      assert(author.name.value === "jhondue")
      assert(author.displayName.value === "JD")
      assert(author.createdAt === 1625065592)
    }
  }

  "ResponseAuthor" should {
    "as JSON" in {
      val expectJson =
        """
          |{
          |  "id" : "01febb8az5t42m2h68xj8c754a",
          |  "name" : "jhondue",
          |  "displayName": "JD",
          |  "createdAt" : 1567814290
          |}
      """.stripMargin.replaceNewlineAndSpace

      val json =
        AuthorResponseModel(
          id = authorId,
          name = AuthorName("JhonDue"),
          displayName = AuthorDisplayName("JD"),
          createdAt = 1567814290
        ).asJson.replaceNewlineAndSpace

      // NOTE: failed equally compare
      assert(json.contains(expectJson))
    }

    "as JSON Array" in {
      val expectJson =
        """
          |[
          |  {
          |    "id" : "01febb8az5t42m2h68xj8c754a",
          |    "name" : "jhondue",
          |    "displayName": "JD",
          |    "createdAt" : 1567814290
          |  },
          |  {
          |    "id" : "01febb8az5t42m2h68xj8c754b",
          |    "name" : "jhondue2",
          |    "displayName": "JD2",
          |    "createdAt" : 1567814291
          |  }
          |]
      """.stripMargin.replaceNewlineAndSpace

      val json =
        Seq(
          AuthorResponseModel(
            id = authorId,
            name = AuthorName("JhonDue"),
            displayName = AuthorDisplayName("JD"),
            createdAt = 1567814290
          ),
          AuthorResponseModel(
            id = authorId2,
            name = AuthorName("JhonDue2"),
            displayName = AuthorDisplayName("JD2"),
            createdAt = 1567814291
          )
        ).asJson.replaceNewlineAndSpace

      // NOTE: failed equally compare
      assert(json.contains(expectJson))
    }
  }

}
