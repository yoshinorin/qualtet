package net.yoshinorin.qualtet.domains.authors

import net.yoshinorin.qualtet.fixture.unsafe
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
      assert(AuthorName("123AbcDef_-").unsafe.value === "123abcdef_-")
    }
    "invalid value" in {
      val result1 = AuthorName("123AbcDef_-.")
      assert(result1.isLeft)
      assert(result1.left.get.isInstanceOf[InvalidAuthorName])

      val result2 = AuthorName("123AbcDef_-!")
      assert(result2.isLeft)
      assert(result2.left.get.isInstanceOf[InvalidAuthorName])
    }
  }

  "AuthorDisplayName" should {
    "valid value" in {
      assert(AuthorDisplayName("123AbcDef_-").unsafe.value === "123AbcDef_-")
    }
    "invalid value" in {
      val result1 = AuthorDisplayName("123AbcDef_-.")
      assert(result1.isLeft)
      assert(result1.left.get.isInstanceOf[InvalidAuthorDisplayName])

      val result2 = AuthorDisplayName("123AbcDef_-!")
      assert(result2.isLeft)
      assert(result2.left.get.isInstanceOf[InvalidAuthorDisplayName])
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

  "AuthorName.unsafe" should {
    "normalize to lowercase" in {
      val name = AuthorName.unsafe("JohnDoe")
      assert(name.value === "johndoe")
    }

    "handle already lowercase input" in {
      val name = AuthorName.unsafe("johndoe")
      assert(name.value === "johndoe")
    }

    "skip validation for invalid characters" in {
      val name = AuthorName.unsafe("invalid@name")
      assert(name.value === "invalid@name")
    }
  }

  "AuthorDisplayName.unsafe" should {
    "not modify the input" in {
      val displayName = AuthorDisplayName.unsafe("JohnDoe")
      assert(displayName.value === "JohnDoe")
    }

    "skip validation for invalid characters" in {
      val displayName = AuthorDisplayName.unsafe("invalid@name")
      assert(displayName.value === "invalid@name")
    }
  }

  "Author" should {
    "default instance" in {
      val currentUTCDateTime = ZonedDateTime.now(ZoneOffset.UTC)
      val author = Author(
        name = AuthorName("JhonDue").unsafe,
        displayName = AuthorDisplayName("JD").unsafe,
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
        AuthorName("JhonDue").unsafe,
        AuthorDisplayName("JD").unsafe,
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
          name = AuthorName("JhonDue").unsafe,
          displayName = AuthorDisplayName("JD").unsafe,
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
            name = AuthorName("JhonDue").unsafe,
            displayName = AuthorDisplayName("JD").unsafe,
            createdAt = 1567814290
          ),
          AuthorResponseModel(
            id = authorId2,
            name = AuthorName("JhonDue2").unsafe,
            displayName = AuthorDisplayName("JD2").unsafe,
            createdAt = 1567814291
          )
        ).asJson.replaceNewlineAndSpace

      // NOTE: failed equally compare
      assert(json.contains(expectJson))
    }
  }

}
