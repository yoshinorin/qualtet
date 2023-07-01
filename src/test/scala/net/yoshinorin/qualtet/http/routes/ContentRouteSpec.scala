package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import org.typelevel.ci.*
import net.yoshinorin.qualtet.auth.RequestToken
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.domains.contents.{ContentId, Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.Modules.*
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.ContentRouteSpec
class ContentRouteSpec extends AnyWordSpec {

  val validAuthor: ResponseAuthor = authorService.findByName(author.name).unsafeRunSync().get
  val validToken: String = authService.generateToken(RequestToken(validAuthor.id, "pass")).unsafeRunSync().token
  val client: Client[IO] = Client.fromHttpApp(router.routes)

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
      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/contents/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === Created)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            // TODO: assert response
          }
        }
        .unsafeRunSync()
    }

    "be delete a content" in {
      val content = contentService.findByPath(Path("/test/ContentRouteSpec1")).unsafeRunSync().get

      // 204 (first time)
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/contents/${content.id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === NoContent)
          }
        }
        .unsafeRunSync()

      // 404 (second time)
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/contents/${content.id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === NotFound)
          }
        }
        .unsafeRunSync()
    }

    "be return 404 DELETE endopoint" in {
      val id = ContentId(generateUlid())

      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/contents/${id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === NotFound)
          }
        }
        .unsafeRunSync()
    }

    "be reject DELETE endpoint caused by invalid token" ignore {
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/contents/reject")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer invalid token"))
          )
        )
        .use { response =>
          IO {
            // TODO: assert(response.status === Unauthorized)
          }
        }
      // TODO: .unsafeRunSync()
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

      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/contents/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
          }
        }
        .unsafeRunSync()
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

      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/contents/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
          }
        }
        .unsafeRunSync()
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

      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/contents/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
          }
        }
        .unsafeRunSync()
    }

    "be reject caused by expired token" ignore {
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

      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/contents/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + expiredToken)), entity = entity))
        .use { response =>
          IO {
            // TODO: assert(response.status === Unauthorized)
          }
        }
      // TODO: .unsafeRunSync()
    }

    "be reject POST endpoint caused by the authorization header is empty" ignore {
      client
        .run(Request(method = Method.POST, uri = uri"/contents/"))
        .use { response =>
          IO {
            // TODO: assert(response.status === Unauthorized)
          }
        }
      // TODO: .unsafeRunSync()
    }

    "be reject POST endpoint caused by invalid token" ignore {
      val entity = EntityEncoder[IO, String].toEntity("")
      client
        .run(
          Request(method = Method.POST, uri = uri"/contents/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + "invalid Token")), entity = entity)
        )
        .use { response =>
          IO {
            // TODO: assert(response.status === Unauthorized)
          }
        }
      // TODO: .unsafeRunSync()
    }

    "be return user not found" ignore {
      val entity = EntityEncoder[IO, String].toEntity("")
      client
        .run(
          Request(
            method = Method.POST,
            uri = uri"/contents/",
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + nonExistsUserToken)),
            entity = entity
          )
        )
        .use { response =>
          IO {
            // TODO: assert(response.status === Unauthorized)
          }
        }
      // TODO: .unsafeRunSync()
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

      val entity = EntityEncoder[IO, String].toEntity(wrongJsonFormat)
      client
        .run(Request(method = Method.POST, uri = uri"/contents/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
          }
        }
        .unsafeRunSync()
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

      val entity = EntityEncoder[IO, String].toEntity(wrongJsonFormat)
      client
        .run(Request(method = Method.POST, uri = uri"/contents/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
          }
        }
        .unsafeRunSync()
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
            tags = List("ContentRoute"),
            externalResources = List()
          )
        )
        .unsafeRunSync()

      client
        .run(
          Request(
            method = Method.GET,
            uri = uri"/contents/test/content/route/spec/2"
          )
        )
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            // TODO: assert json
          }
        }
        .unsafeRunSync()
    }

    /*
      TODO: should be pass with trailing slash
      Get("/contents/test/content/route/spec/2/") ~> contentRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        // TODO: assert json
      }
     */

    // 401 Invalid JWT with POST

    /* TODO
      The current source code returns 500. But should be return 404.
    "be return 404 with non-trailing slash" in {
      client
        .run(
          Request(
            method = Method.GET,
            uri = uri"/contents/this/is/a/404"
          )
        )
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
          }
        }
        .unsafeRunSync()
    }

    "be return 404 with trailing slash" in {
      client
        .run(
          Request(
            method = Method.GET,
            uri = uri"/contents/this/is/a/404/"
          )
        )
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
          }
        }
        .unsafeRunSync()
    }
     */

    "return method not allowed" in {
      client
        .run(
          Request(
            method = Method.PATCH,
            uri = uri"/contents/this/is/a/404/",
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === MethodNotAllowed)
          }
        }
        .unsafeRunSync()
    }

  }

}
