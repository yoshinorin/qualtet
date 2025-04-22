package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.domains.authors.AuthorId
import net.yoshinorin.qualtet.domains.contents.{ContentId, ContentPath}
import net.yoshinorin.qualtet.domains.externalResources.{ExternalResourceKind, ExternalResources}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.tags.{Tag, TagId, TagName, TagPath}
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.domains.errors.{ContentTitleRequired, HtmlContentRequired, InvalidPath, RawContentRequired}
import org.scalatest.wordspec.AnyWordSpec

import java.time.{Instant, ZoneOffset, ZonedDateTime}

// testOnly net.yoshinorin.qualtet.domains.contents.ContentSpec
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

  "ContentPath" should {

    "appllicable with prefix slush" in {
      val pathString = "/this-is-a-pathあいうえお/q=あ%E3%80%80い&bar"
      val path = ContentPath.apply(pathString)

      assert(path.isInstanceOf[ContentPath])
      assert(path.value === pathString)
    }

    "appllicable without prefix slush" in {
      val pathString = "this-is-a-path"
      val path = ContentPath.apply(pathString)

      assert(path.isInstanceOf[ContentPath])
      assert(path.value === "/" + pathString)
    }

    "throw InvalidPath exception with invalid characters" in {
      assertThrows[InvalidPath] {
        ContentPath.apply("this-is-a-path\u0000")
      }
    }
  }

  "Content" should {
    "default instance" in {
      val currentUTCDateTime = ZonedDateTime.now(ZoneOffset.UTC)
      val content = Content(
        authorId = AuthorId.apply(),
        contentTypeId = contentTypeId,
        path = ContentPath("/path"),
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

  "RequestContent" should {
    "thrown ContentTitleRequired if title is empty" in {
      assertThrows[ContentTitleRequired] {
        ContentRequestModel(
          contentType = "article",
          path = ContentPath("/articles/contentSpec/1"),
          title = "",
          rawContent = "this is a articleRoute raw content",
          htmlContent = "this is a articleRoute html content",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          tags = List(),
          externalResources = List()
        )
      }
    }

    "thrown RawContentRequired if rawContent is empty" in {
      assertThrows[RawContentRequired] {
        ContentRequestModel(
          contentType = "article",
          path = ContentPath("/articles/contentSpec/2"),
          title = "this is a articleRoute title",
          rawContent = "",
          htmlContent = "this is a articleRoute html content",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          tags = List(),
          externalResources = List()
        )
      }
    }

    "thrown HtmlContentRequired if htmlContent is empty" in {
      assertThrows[HtmlContentRequired] {
        ContentRequestModel(
          contentType = "article",
          path = ContentPath("/articles/contentSpec/3"),
          title = "this is a articleRoute title",
          rawContent = "this is a articleRoute raw content",
          htmlContent = "",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          tags = List(),
          externalResources = List()
        )
      }
    }
  }

  "ResponseContent" should {
    "as JSON" in {
      val expectJson =
        """
          |{
          |  "id": "01h08dm4th59wsk81d4h96cf6b",
          |  "title" : "title",
          |  "robotsAttributes" : "noarchive, noimageindex",
          |  "externalResources" : [],
          |  "tags" : [],
          |  "description" : "this is a description",
          |  "content" : "this is a content",
          |  "length" : 17,
          |  "authorName" : "jhondue",
          |  "publishedAt" : 1567814290,
          |  "updatedAt" : 1567814291
          |}
      """.stripMargin.replaceNewlineAndSpace

      val json =
        ContentDetailResponseModel(
          id = ContentId("01h08dm4th59wsk81d4h96cf6b"),
          title = "title",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          description = "this is a description",
          content = "this is a content",
          length = "this is a content".length,
          authorName = author.name,
          publishedAt = 1567814290,
          updatedAt = 1567814291
        ).asJson.replaceNewlineAndSpace

      // NOTE: failed equally compare
      assert(json.contains(expectJson))
    }

    "as JSON Array" in {
      val expectJson =
        """
          |[
          |  {
          |    "id": "01h08dm4th59wsk81d4h96cf6b",
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
          |        "name": "content-spec1",
          |        "path": "content-spec1-path"
          |      },
          |      {
          |        "id": "01frdbe1g83533h92rkhy8ctkw",
          |        "name": "content-spec2",
          |        "path": "content-spec2-path"
          |      }
          |    ],
          |    "description" : "this is a description1",
          |    "content" : "this is a content1",
          |    "length" : 18,
          |    "authorName" : "jhondue",
          |    "publishedAt" : 1567814290,
          |    "updatedAt" : 1567814299
          |  },
          |  {
          |    "id": "01h08dm4th59wsk81d4h96cf6c",
          |    "title" : "title2",
          |    "robotsAttributes" : "all",
          |    "externalResources" : [],
          |    "tags" : [],
          |    "description" : "this is a description2",
          |    "content" : "this is a content2",
          |    "length" : 18,
          |    "authorName" : "jhondue",
          |    "publishedAt" : 1567814291,
          |    "updatedAt" : 1567814391
          |  }
          |]
      """.stripMargin.replaceNewlineAndSpace

      val json =
        Seq(
          ContentDetailResponseModel(
            id = ContentId("01h08dm4th59wsk81d4h96cf6b"),
            title = "title1",
            robotsAttributes = Attributes("noarchive, noimageindex"),
            externalResources = List(
              ExternalResources(
                ExternalResourceKind("css"),
                List("css1", "css2")
              ),
              ExternalResources(
                ExternalResourceKind("js"),
                List("js1", "js2")
              )
            ),
            tags = List(
              Tag(TagId("01frdbdsdty42fv147cerqpv73"), TagName("content-spec1"), TagPath("content-spec1-path")),
              Tag(TagId("01frdbe1g83533h92rkhy8ctkw"), TagName("content-spec2"), TagPath("content-spec2-path"))
            ),
            description = "this is a description1",
            content = "this is a content1",
            length = "this is a content1".length,
            authorName = author.name,
            publishedAt = 1567814290,
            updatedAt = 1567814299
          ),
          ContentDetailResponseModel(
            id = ContentId("01h08dm4th59wsk81d4h96cf6c"),
            title = "title2",
            robotsAttributes = Attributes("all"),
            externalResources = List(),
            description = "this is a description2",
            content = "this is a content2",
            length = "this is a content2".length,
            authorName = author.name,
            publishedAt = 1567814291,
            updatedAt = 1567814391
          )
        ).asJson.replaceNewlineAndSpace

      // NOTE: failed equally compare
      assert(json.contains(expectJson))
    }
  }

}
