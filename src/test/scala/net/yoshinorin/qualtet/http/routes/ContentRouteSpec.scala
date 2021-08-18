package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import net.yoshinorin.qualtet.domains.models.authors.{AuthorId, AuthorName}
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.models.contents.{Content, Path, RequestContent, ResponseContent}
import net.yoshinorin.qualtet.domains.services.ContentService
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.http.routes.ContentRouteSpec
class ContentRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val mockContentService: ContentService = Mockito.mock(classOf[ContentService])
  val contentRoute: ContentRoute = new ContentRoute(mockContentService)

  // POST
  when(
    mockContentService.createContentFromRequest(
      RequestContent(
        authorName = AuthorName("JhonDue"),
        contentType = "article",
        path = Path("/test/path"),
        title = "this is a title",
        rawContent = "this is a raw content"
      )
    )
  ).thenReturn(
    IO(
      Content(
        authorId = new AuthorId,
        contentTypeId = new ContentTypeId,
        path = Path("/test/path/"),
        title = "this is a title",
        rawContent = "this is a raw content",
        htmlContent = "this is a html content"
      )
    )
  )

  // GET
  when(
    mockContentService.findByPath(Path("/this/is/a/example/"))
  ).thenReturn(
    IO(
      Option(
        Content(
          authorId = new AuthorId,
          contentTypeId = new ContentTypeId,
          path = Path("/this/is/a/example/"),
          title = "this is a title",
          rawContent = "raw content",
          htmlContent = "html content",
          publishedAt = 1567814290
        )
      )
    )
  )

  // GET
  when(
    mockContentService.findByPath(Path("/this/is/a/404/"))
  ).thenReturn(
    IO(None)
  )

  "ContentRoute" should {
    /*
    TODO: fix test case
    "success create content" in {
      val json =
        """
          |{
          |  "authorName" : "JhonDue",
          |  "contentType" : "article",
          |  "path" : "/test/path",
          |  "title" : "this is a title",
          |  "rawContent" : "this is a raw content"
          |}
        """.stripMargin

      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, json) ~> contentRoute.route ~> check {
        assert(status == StatusCodes.Created)
        assert(contentType == ContentTypes.`application/json`)
      }
    }
     */

    "reject with bad request (wrong JSON format)" in {
      val wrongJsonFormat =
        """
          |{
          |  "authorName" : "JhonDue",
          |  "contentType" : "article",
          |  "path" : "/test/path",
          |  "title" : "this is a title"
          |  "rawContent" : "this is a raw content"
          |}
        """.stripMargin

      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, wrongJsonFormat) ~> contentRoute.route ~> check {
        assert(status == StatusCodes.BadRequest)
        // TODO: assert response json
      }
    }

    "reject with bad request (lack of JSON key)" in {
      val wrongJsonFormat =
        """
          |{
          |  "authorName" : "JhonDue",
          |  "contentType" : "article",
          |  "path" : "/test/path",
          |  "title" : "this is a title"
          |}
        """.stripMargin

      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, wrongJsonFormat) ~> contentRoute.route ~> check {
        assert(status == StatusCodes.BadRequest)
        // TODO: assert response json
      }
    }

    "return specific content" in {
      val expectJson =
        """
          |{
          |  "title" : "this is a title",
          |  "content" : "html content",
          |  "publishedAt" : 1567814290
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      Get("/contents/this/is/a/example/") ~> contentRoute.route ~> check {
        assert(status == StatusCodes.OK)
        assert(contentType == ContentTypes.`application/json`)
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "") == expectJson)
      }
    }

    "return 404" in {
      Get("/contents/this/is/a/404/") ~> contentRoute.route ~> check {
        assert(status == StatusCodes.NotFound)
        assert(contentType == ContentTypes.`application/json`)
      }
    }

  }

}
