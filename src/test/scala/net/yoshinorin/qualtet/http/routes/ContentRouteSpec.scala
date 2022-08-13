package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.AuthenticationFailedRejection
import akka.http.scaladsl.server.AuthenticationFailedRejection.CredentialsMissing
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.yoshinorin.qualtet.auth.RequestToken
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.fixture.Fixture.{authService, author, authorService, contentRoute, contentService, expiredToken, nonExistsUserToken}
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.http.routes.ContentRouteSpec
class ContentRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val validAuthor: ResponseAuthor = authorService.findByName(author.name).unsafeRunSync().get
  val validToken: String = authService.generateToken(RequestToken(validAuthor.id, "pass")).unsafeRunSync().token

  "ContentRoute" should {
    "be create a content" in {
      val json =
        """
          |{
          |  "contentType" : "article",
          |  "path" : "/test/ContentRouteSpec1",
          |  "title" : "this is a ContentRouteSpec1 title",
          |  "robotsAttributes": "noarchive, noimageindex",
          |  "rawContent" : "this is a raw ContentRouteSpec1",
          |  "htmlContent" : "<p>this is a html ContentRouteSpec1<p>",
          |  "publishedAt" : 1644075206,
          |  "updatedAt" : 1644075206
          |}
        """.stripMargin

      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, json) ~> addCredentials(OAuth2BearerToken(validToken)) ~> contentRoute.route ~> check {
        assert(status === StatusCodes.Created)
        assert(contentType === ContentTypes.`application/json`)
      }
    }

    "be delete a content" in {
      val content = contentService.findByPath(Path("/test/ContentRouteSpec1")).unsafeRunSync().get
      Delete(s"/contents/${content.id.value}")
        .addCredentials(OAuth2BearerToken(validToken)) ~> contentRoute.route ~> check {
        assert(status === StatusCodes.NoContent)
      }
      assert(contentService.findByPath(Path("/test/ContentRouteSpec1")).unsafeRunSync().isEmpty)
    }

    "be return 400 BadRequest caused by empty title" in {
      val json =
        """
          |{
          |  "contentType" : "article",
          |  "path" : "/test/ContentRouteSpec/BadRequest1",
          |  "title" : "",
          |  "robotsAttributes": "noarchive, noimageindex",
          |  "rawContent" : "this is a raw ContentRouteSpec BadRequest1",
          |  "htmlContent" : "<p>this is a html ContentRouteSpec BadRequest1<p>",
          |  "publishedAt" : 1644075206,
          |  "updatedAt" : 1644075206
          |}
        """.stripMargin

      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, json) ~> addCredentials(OAuth2BearerToken(validToken)) ~> contentRoute.route ~> check {
        assert(status === StatusCodes.BadRequest)
        assert(contentType === ContentTypes.`application/json`)
      }
    }

    "be return 400 BadRequest caused by empty rawContent" in {
      val json =
        """
          |{
          |  "contentType" : "article",
          |  "path" : "/test/ContentRouteSpec/BadRequest2",
          |  "title" : "this is a ContentRouteSpec title BadRequest2",
          |  "robotsAttributes": "noarchive, noimageindex",
          |  "rawContent" : "",
          |  "htmlContent" : "<p>this is a html ContentRouteSpec BadRequest2<p>",
          |  "publishedAt" : 1644075206,
          |  "updatedAt" : 1644075206
          |}
        """.stripMargin

      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, json) ~> addCredentials(OAuth2BearerToken(validToken)) ~> contentRoute.route ~> check {
        assert(status === StatusCodes.BadRequest)
        assert(contentType === ContentTypes.`application/json`)
      }
    }

    "be return 400 BadRequest caused by empty htmlContent" in {
      val json =
        """
          |{
          |  "contentType" : "article",
          |  "path" : "/test/ContentRouteSpec/BadRequest3",
          |  "title" : "this is a ContentRouteSpec title BadRequest3",
          |  "robotsAttributes": "noarchive, noimageindex",
          |  "rawContent" : "this is a raw ContentRouteSpec BadRequest3",
          |  "htmlContent" : "",
          |  "publishedAt" : 1644075206,
          |  "updatedAt" : 1644075206
          |}
        """.stripMargin

      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, json) ~> addCredentials(OAuth2BearerToken(validToken)) ~> contentRoute.route ~> check {
        assert(status === StatusCodes.BadRequest)
        assert(contentType === ContentTypes.`application/json`)
      }
    }

    "be reject caused by expired token" in {
      val json =
        """
          |{
          |  "contentType" : "article",
          |  "path" : "/test/ContentRouteSpecExpiredToken",
          |  "title" : "this is a ContentRouteSpecExpiredToken title",
          |  "robotsAttributes": "noarchive, noimageindex",
          |  "rawContent" : "this is a raw ContentRouteSpecExpiredToken",
          |  "htmlContent" : "<p>this is a html ContentRouteSpecExpiredToken<p>",
          |  "publishedAt" : 1644075206,
          |  "updatedAt" : 1644075206
          |}
        """.stripMargin

      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, json) ~> addCredentials(OAuth2BearerToken(expiredToken)) ~> contentRoute.route ~> check {
        // TODO: fix status code
        assert(status === StatusCodes.InternalServerError)
      }
    }

    "be reject caused by the authorization header is empty" in {
      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, """{}""") ~> contentRoute.route ~> check {
        assert(rejection.asInstanceOf[AuthenticationFailedRejection].cause === CredentialsMissing)
      }
    }

    "be reject caused by invalid token" in {
      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, """{}""") ~> addCredentials(OAuth2BearerToken("invalid token")) ~> contentRoute.route ~> check {
        // TODO: fix status code
        assert(status === StatusCodes.InternalServerError)
      }
    }

    "be return user not found" in {
      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, """{}""") ~> addCredentials(OAuth2BearerToken(nonExistsUserToken)) ~> contentRoute.route ~> check {
        // TODO: fix status code
        assert(status === StatusCodes.InternalServerError)
      }
    }

    "be reject with bad request (wrong JSON format)" in {
      val wrongJsonFormat =
        """
          |{
          |  "contentType" : "article"
          |  "path" : "/test/ContentRouteSpec1",
          |  "title" : "this is a ContentRouteSpec1 title",
          |  "robotsAttributes": "noarchive, noimageindex",
          |  "rawContent" : "this is a raw ContentRouteSpec1",
          |  "htmlContent" : "<p>this is a html ContentRouteSpec1<p>",
          |  "publishedAt" : 1644075206,
          |  "updatedAt" : 1644075206
          |}
        """.stripMargin

      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, wrongJsonFormat) ~> addCredentials(OAuth2BearerToken(validToken)) ~> contentRoute.route ~> check {
        assert(status === StatusCodes.BadRequest)
        // TODO: assert response json
      }
    }

    "be reject with bad request (lack of JSON key)" in {
      val wrongJsonFormat =
        """
          |{
          |  "contentType" : "article",
          |  "path" : "/test/path",
          |  "title" : "this is a title"
          |}
        """.stripMargin

      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, wrongJsonFormat) ~> addCredentials(OAuth2BearerToken(validToken)) ~> contentRoute.route ~> check {
        assert(status === StatusCodes.BadRequest)
        // TODO: assert response json
      }
    }

    "be return specific content" in {
      // NOTE: create content and related data for test
      contentService
        .createContentFromRequest(
          validAuthor.name,
          RequestContent(
            contentType = "article",
            path = Path("/test/content/route/spec/2"),
            title = "this is a ContentRouteSpec2 title",
            rawContent = "this is a raw ContentRouteSpec2",
            htmlContent = "<p>this is a html ContentRouteSpec2<p>",
            robotsAttributes = Attributes("noarchive, noimageindex"),
            tags = Option(List("ContentRoute")),
            externalResources = Option(List())
          )
        )
        .unsafeRunSync()

      Get("/contents/test/content/route/spec/2") ~> contentRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        // TODO: assert json
      }

      /*
      TODO: should be pass with trailing slash
      Get("/contents/test/content/route/spec/2/") ~> contentRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        // TODO: assert json
      }
     */
    }

    // 401 Invalid JWT with POST

    "be return 404 with non-trailing slash" in {
      Get("/contents/this/is/a/404") ~> contentRoute.route ~> check {
        assert(status === StatusCodes.NotFound)
        assert(contentType === ContentTypes.`application/json`)
      }
    }

    "be return 404 with trailing slash" in {
      Get("/contents/this/is/a/404/") ~> contentRoute.route ~> check {
        assert(status === StatusCodes.NotFound)
        assert(contentType === ContentTypes.`application/json`)
      }
    }

  }

}
