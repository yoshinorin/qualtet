package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.fixture.unsafe
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
      val pathString = "/this-is-a-pathあいうえお/あ%E3%20%20いbar"
      val path = ContentPath.apply(pathString)

      assert(path.isRight)
      assert(path.unsafe.value === pathString)
    }

    "appllicable without prefix slush" in {
      val pathString = "this-is-a-path"
      val path = ContentPath.apply(pathString)

      assert(path.isRight)
      assert(path.unsafe.value === "/" + pathString)
    }

    "accept paths with valid characters" in {
      val validPaths = List(
        "/valid/path",
        "/valid/path-with-hyphen",
        "/valid/path_with_underscore",
        "/valid/path.with.dots",
        "/japanese/path/日本語",
        "/path/with/numbers/123",
        "/path with spaces"
      )

      validPaths.foreach { path =>
        val contentPath = ContentPath(path)
        assert(contentPath.isRight)
        assert(contentPath.unsafe.value === path)
      }
    }

    "throw InvalidPath exception with invalid characters" in {
      val invalidPaths = List(
        "/invalid/path:with:colon",
        "/invalid/path?with?question",
        "/invalid/path#with#hash",
        "/invalid/path@with@at",
        "/invalid/path!with!exclamation",
        "/invalid/path$with$dollar",
        "/invalid/path&with&ampersand",
        "/invalid/path'with'quote",
        "/invalid/path*with*asterisk",
        "/invalid/path+with+plus",
        "/invalid/path;with;semicolon",
        "/invalid/path=with=equals",
        "/invalid/path<with<less",
        "/invalid/path>with>greater",
        "/invalid/path\"with\"quote",
        "/invalid/path\\with\\backslash",
        "/invalid/path^with^caret",
        "/invalid/path`with`backtick",
        "/invalid/path{with{brace",
        "/invalid/path}with}brace",
        "/invalid/path|with|pipe",
        "/invalid/path~with~tilde"
      )

      invalidPaths.foreach { path =>
        val result = ContentPath(path)
        assert(result.isLeft)
        assert(result.left.get.detail === s"Invalid character contains: ${path}")
      }
    }

    "throw InvalidPath exception with invalid percent encoding" in {
      val invalidEncodedPaths = List(
        "/invalid/path/with%",
        "/invalid/path/with%1",
        "/invalid/path/with%XX",
        "/invalid/path/with%ZZ"
      )

      invalidEncodedPaths.foreach { path =>
        val result = ContentPath(path)
        assert(result.isLeft)
        assert(result.left.get.detail === s"Invalid percent encoding in path: ${path}")
      }
    }

    "accept paths with contains reserved path partially" in {
      val validPaths = List(
        "/adjacent_after/",
        "/ADJACENT_after/",
        "/before_adjacent/",
        "/before_adjacent_after/"
      )

      validPaths.foreach { path =>
        val contentPath = ContentPath(path)
        assert(contentPath.isRight)
        assert(contentPath.unsafe.value === path)
      }
    }

    "throw InvalidPath exception with reserved path" in {
      val containReservedPaths = List(
        "/adjacent/",
        "/adjacent",
        "/ADJACENT/",
        "adjacent",
        "/before/adjacent/",
        "/adjacent/after",
        "/before/adjacent/after",
        "/system"
      )

      containReservedPaths.foreach { path =>
        val result = ContentPath(path)
        assert(result.isLeft)
        assert(result.left.get.detail === s"Path contains reserved word: ${path}")
      }
    }

    "accept valid percent encoding" in {
      val validEncodedPaths = List(
        "/valid/path/with%20space",
        "/valid/path/with%2Fencoded%2Fslash",
        "/valid/path/with%E3%81%82", // "あ" in UTF-8
        "/valid/path/with%3A%3F%23encoded%40special%21chars"
      )

      validEncodedPaths.foreach { path =>
        val contentPath = ContentPath(path)
        assert(contentPath.isRight)
        assert(contentPath.unsafe.value === path)
      }
    }

    "add leading slash to path when missing" in {
      val pathWithoutSlash = "path/without/leading/slash"
      val contentPath = ContentPath(pathWithoutSlash)
      assert(contentPath.isRight)
      assert(contentPath.unsafe.value === s"/${pathWithoutSlash}")
    }
  }

  "ContentPath.unsafe" should {
    "normalize path by adding leading slash" in {
      val path = ContentPath.unsafe("test/path")
      assert(path.value === "/test/path")
    }

    "not add leading slash if already present" in {
      val path = ContentPath.unsafe("/test/path")
      assert(path.value === "/test/path")
    }

    "skip validation for invalid characters" in {
      val path = ContentPath.unsafe("invalid:path")
      assert(path.value === "/invalid:path")
    }

    "skip validation for reserved words" in {
      val path = ContentPath.unsafe("admin")
      assert(path.value === "/admin")
    }

    "skip validation for invalid percent encoding" in {
      val path = ContentPath.unsafe("test%")
      assert(path.value === "/test%")
    }
  }

  "Content" should {
    "default instance" in {
      val currentUTCDateTime = ZonedDateTime.now(ZoneOffset.UTC)
      val content = Content(
        authorId = AuthorId.apply(),
        contentTypeId = contentTypeId,
        path = ContentPath("/path").unsafe,
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
          path = ContentPath("/articles/contentSpec/1").unsafe,
          title = "",
          rawContent = "this is a articleRoute raw content",
          htmlContent = "this is a articleRoute html content",
          robotsAttributes = Attributes("noarchive, noimageindex").unsafe,
          tags = List(),
          externalResources = List()
        )
      }
    }

    "thrown RawContentRequired if rawContent is empty" in {
      assertThrows[RawContentRequired] {
        ContentRequestModel(
          contentType = "article",
          path = ContentPath("/articles/contentSpec/2").unsafe,
          title = "this is a articleRoute title",
          rawContent = "",
          htmlContent = "this is a articleRoute html content",
          robotsAttributes = Attributes("noarchive, noimageindex").unsafe,
          tags = List(),
          externalResources = List()
        )
      }
    }

    "thrown HtmlContentRequired if htmlContent is empty" in {
      assertThrows[HtmlContentRequired] {
        ContentRequestModel(
          contentType = "article",
          path = ContentPath("/articles/contentSpec/3").unsafe,
          title = "this is a articleRoute title",
          rawContent = "this is a articleRoute raw content",
          htmlContent = "",
          robotsAttributes = Attributes("noarchive, noimageindex").unsafe,
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
          robotsAttributes = Attributes("noarchive, noimageindex").unsafe,
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
          |        "path": "/content-spec1-path"
          |      },
          |      {
          |        "id": "01frdbe1g83533h92rkhy8ctkw",
          |        "name": "content-spec2",
          |        "path": "/content-spec2-path"
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
            robotsAttributes = Attributes("noarchive, noimageindex").unsafe,
            externalResources = List(
              ExternalResources(
                ExternalResourceKind("css").unsafe,
                List("css1", "css2")
              ),
              ExternalResources(
                ExternalResourceKind("js").unsafe,
                List("js1", "js2")
              )
            ),
            tags = List(
              Tag(TagId("01frdbdsdty42fv147cerqpv73"), TagName("content-spec1"), TagPath("content-spec1-path").unsafe),
              Tag(TagId("01frdbe1g83533h92rkhy8ctkw"), TagName("content-spec2"), TagPath("content-spec2-path").unsafe)
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
            robotsAttributes = Attributes("all").unsafe,
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
