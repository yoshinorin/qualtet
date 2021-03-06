package net.yoshinorin.qualtet.domains.contents

import io.circe.syntax._
import net.yoshinorin.qualtet.domains.authors.AuthorId
import net.yoshinorin.qualtet.domains.contents.{Content, ContentId, Path, ResponseContent}
import net.yoshinorin.qualtet.domains.externalResources.{ExternalResourceKind, ExternalResources}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.tags.{Tag, TagId, TagName}
import net.yoshinorin.qualtet.fixture.Fixture._
import org.scalatest.wordspec.AnyWordSpec

import java.time.{Instant, ZoneOffset, ZonedDateTime}

// testOnly net.yoshinorin.qualtet.domains.models.contents.ContentSpec
class ContentSpec extends AnyWordSpec {

  "ContentId" should {
    "create instance with specific id" in {
      assert(contentId.value === "01febb1333pd3431q1a1e00fbt")
    }

    "can not create instance" in {
      assertThrows[IllegalArgumentException] {
        ContentId("not-a-ULID")
      }
    }
  }

  "Path" should {
    "can create instance" in {
      assert(Path("/test/path").value === "/test/path")
    }

    "add slash on the top" in {
      assert(Path("test/path").value === "/test/path")
    }
  }

  "Content" should {
    "default instance" in {
      val currentUTCDateTime = ZonedDateTime.now(ZoneOffset.UTC)
      val content = Content(
        authorId = new AuthorId,
        contentTypeId = contentTypeId,
        path = Path("/path"),
        title = "",
        rawContent = "",
        htmlContent = ""
      )
      val instanceUTCDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(content.publishedAt), ZoneOffset.UTC)

      assert(content.id.isInstanceOf[ContentId])
      assert(instanceUTCDateTime.getYear === currentUTCDateTime.getYear)
      assert(instanceUTCDateTime.getMonth === currentUTCDateTime.getMonth)
      assert(instanceUTCDateTime.getDayOfMonth === currentUTCDateTime.getDayOfMonth)
      assert(instanceUTCDateTime.getHour === currentUTCDateTime.getHour)
    }
  }

  "ResponseContent" should {
    "as JSON" in {
      val expectJson =
        """
          |{
          |  "title" : "title",
          |  "robotsAttributes" : "noarchive, noimageindex",
          |  "externalResources" : null,
          |  "tags" : null,
          |  "description" : "this is a description",
          |  "content" : "this is a content",
          |  "authorName" : "jhondue",
          |  "publishedAt" : 1567814290,
          |  "updatedAt" : 1567814291
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      val json = ResponseContent(
        title = "title",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        description = "this is a description",
        content = "this is a content",
        authorName = author.name,
        publishedAt = 1567814290,
        updatedAt = 1567814291
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
          |    "externalResources" : [
          |      {
          |        "kind": "css",
          |        "values": ["css1","css2"]
          |      },
          |      {
          |        "kind": "js",
          |        "values": ["js1","js2"]
          |      }
          |    ],
          |    "tags" : [
          |      {
          |        "id": "01frdbdsdty42fv147cerqpv73",
          |        "name": "ABC"
          |      },
          |      {
          |        "id": "01frdbe1g83533h92rkhy8ctkw",
          |        "name": "DEF"
          |      }
          |    ],
          |    "description" : "this is a description1",
          |    "content" : "this is a content1",
          |    "authorName" : "jhondue",
          |    "publishedAt" : 1567814290,
          |    "updatedAt" : 1567814299
          |  },
          |  {
          |    "title" : "title2",
          |    "robotsAttributes" : "all",
          |    "externalResources" : null,
          |    "tags" : null,
          |    "description" : "this is a description2",
          |    "content" : "this is a content2",
          |    "authorName" : "jhondue",
          |    "publishedAt" : 1567814291,
          |    "updatedAt" : 1567814391
          |  }
          |]
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      val json = Seq(
        ResponseContent(
          title = "title1",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          externalResources = Option(
            List(
              ExternalResources(
                ExternalResourceKind("css"),
                List("css1", "css2")
              ),
              ExternalResources(
                ExternalResourceKind("js"),
                List("js1", "js2")
              )
            )
          ),
          tags = Option(
            List(
              Tag(TagId("01frdbdsdty42fv147cerqpv73"), TagName("ABC")),
              Tag(TagId("01frdbe1g83533h92rkhy8ctkw"), TagName("DEF"))
            )
          ),
          description = "this is a description1",
          content = "this is a content1",
          authorName = author.name,
          publishedAt = 1567814290,
          updatedAt = 1567814299
        ),
        ResponseContent(
          title = "title2",
          robotsAttributes = Attributes("all"),
          externalResources = None,
          description = "this is a description2",
          content = "this is a content2",
          authorName = author.name,
          publishedAt = 1567814291,
          updatedAt = 1567814391
        )
      ).asJson.toString.replaceAll("\n", "").replaceAll(" ", "")

      //NOTE: failed equally compare
      assert(json.contains(expectJson))
    }
  }

}
