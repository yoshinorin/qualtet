package net.yoshinorin.qualtet.domains.models.contents

import io.circe.syntax._
import net.yoshinorin.qualtet.domains.models.Fail.UnprocessableEntity
import net.yoshinorin.qualtet.domains.models.authors.AuthorId
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.models.robots.Attributes
import org.scalatest.wordspec.AnyWordSpec

import java.time.{Instant, ZoneOffset, ZonedDateTime}

// testOnly net.yoshinorin.qualtet.domains.models.contents.ContentSpec
class ContentSpec extends AnyWordSpec {

  "ContentId" should {
    "create instance with specific id" in {
      assert(ContentId("01febb1333pd3431q1a1e00fbt").value == "01febb1333pd3431q1a1e00fbt")
    }

    "can not create instance" in {
      assertThrows[IllegalArgumentException] {
        ContentId("not-a-ULID")
      }
    }
  }

  "Path" should {
    "can create instance" in {
      assert(Path("/test/path").value == "/test/path")
    }

    "add slash on the top" in {
      assert(Path("test/path").value == "/test/path")
    }
  }

  "Content" should {
    "default instance" in {
      val currentUTCDateTime = ZonedDateTime.now(ZoneOffset.UTC)
      val content = Content(
        authorId = new AuthorId,
        contentTypeId = ContentTypeId("01febb1333pd3431q1a1e00fbt"),
        path = Path("/path"),
        title = "",
        rawContent = "",
        htmlContent = ""
      )
      val instanceUTCDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(content.publishedAt), ZoneOffset.UTC)

      assert(content.id.isInstanceOf[ContentId])
      assert(instanceUTCDateTime.getYear == currentUTCDateTime.getYear)
      assert(instanceUTCDateTime.getMonth == currentUTCDateTime.getMonth)
      assert(instanceUTCDateTime.getDayOfMonth == currentUTCDateTime.getDayOfMonth)
      assert(instanceUTCDateTime.getHour == currentUTCDateTime.getHour)
    }
  }

  "ResponseContent" should {
    "as JSON" in {
      val expectJson =
        """
          |{
          |  "title" : "title",
          |  "robotsAttributes" : "noarchive, noimageindex",
          |  "content" : "this is a content",
          |  "publishedAt" : 1567814290
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      val json = ResponseContent(
        title = "title",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        content = "this is a content",
        publishedAt = 1567814290
      ).asJson.toString.replaceAll("\n", "").replaceAll(" ", "")

      //NOTE: failed equally compare
      assert(json.contains(expectJson))
    }

    "as JSON Array" in {
      val expectJson =
        """
          |[
          |  {
          |    "title" : "title1",
          |    "robotsAttributes" : "noarchive, noimageindex",
          |    "content" : "this is a content1",
          |    "publishedAt" : 1567814290
          |  },
          |  {
          |    "title" : "title2",
          |    "robotsAttributes" : "all",
          |    "content" : "this is a content2",
          |    "publishedAt" : 1567814291
          |  }
          |]
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      val json = Seq(
        ResponseContent(
          title = "title1",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          content = "this is a content1",
          publishedAt = 1567814290
        ),
        ResponseContent(
          title = "title2",
          robotsAttributes = Attributes("all"),
          content = "this is a content2",
          publishedAt = 1567814291
        )
      ).asJson.toString.replaceAll("\n", "").replaceAll(" ", "")

      //NOTE: failed equally compare
      assert(json.contains(expectJson))
    }
  }

}
