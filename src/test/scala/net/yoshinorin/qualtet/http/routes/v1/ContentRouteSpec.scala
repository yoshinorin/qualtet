package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import org.typelevel.ci.*
import net.yoshinorin.qualtet.auth.RequestToken
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.domains.contents.{ContentDetailResponseModel, ContentId, ContentPath, ContentRequestModel, ContentResponseModel}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.tags.{Tag, TagName, TagPath}
import net.yoshinorin.qualtet.http.errors.ResponseProblemDetails
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec

import java.time.Instant

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.v1.ContentRouteSpec
class ContentRouteSpec extends AnyWordSpec {

  val validAuthor: AuthorResponseModel = authorService.findByName(author.name).unsafeRunSync().get
  val validToken: String = authService.generateToken(RequestToken(validAuthor.id, "pass")).unsafeRunSync().token
  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "ContentRoute" should {
    "create a content (HTTP Header Bearer is UpperCase)" in {
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
        .run(Request(method = Method.POST, uri = uri"/v1/contents/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === Created)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))

            val maybeContent = unsafeDecode[ContentResponseModel](response)
            assert(maybeContent.authorId === validAuthor.id)
            // assert(maybeContent.contentTypeId === 'TODO') TODO: assert contentTypeId
            assert(maybeContent.htmlContent === "<p>this is a html ContentRouteSpec1<p>")
            // assert(maybeContent.id) TODO: asset id is ULID
            assert(maybeContent.path === "/test/ContentRouteSpec1")
            assert(maybeContent.publishedAt === 1644075206)
            assert(maybeContent.rawContent === "this is a raw ContentRouteSpec1")
            assert(maybeContent.title === "this is a ContentRouteSpec1 title")
            assert(maybeContent.updatedAt === 1644075206)
          }
        }
        .unsafeRunSync()
    }

    "create a content (HTTP Header bearer is LowerCase)" in {
      val json =
        """
          |{
          |  "contentType" : "article",
          |  "path" : "/test/ContentRouteSpec1Lower",
          |  "title" : "this is a ContentRouteSpec1Lower title",
          |  "robotsAttributes": "noarchive, noimageindex",
          |  "rawContent" : "this is a raw ContentRouteSpec1Lower",
          |  "htmlContent" : "<p>this is a html ContentRouteSpec1Lower<p>",
          |  "publishedAt" : 1644075206,
          |  "updatedAt" : 1644075206
          |}
        """.stripMargin
      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/v1/contents/", headers = Headers(Header.Raw(ci"Authorization", "bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === Created)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            // NOTE: no-need to assert response. Because, this test case for `HTTP Header bearer is LowerCase`.
          }
        }
        .unsafeRunSync()
    }

    "delete a content" in {
      val content = contentService.findByPath(ContentPath("/test/ContentRouteSpec1")).unsafeRunSync().get

      // 204 (first time)
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/contents/${content.id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === NoContent)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()

      // 404 (second time)
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/contents/${content.id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail.startsWith("content not found: "))
            assert(maybeError.instance === s"/v1/contents/${content.id.value}")
          }
        }
        .unsafeRunSync()
    }

    "return 404 DELETE endopoint" in {
      val id = ContentId(generateUlid())

      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/contents/${id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail === s"content not found: ${id.value}")
            assert(maybeError.instance === s"/v1/contents/${id.value}")
          }
        }
        .unsafeRunSync()
    }

    "reject DELETE endpoint caused by invalid token" in {
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/contents/reject")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer invalid token"))
          )
        )
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "return 400 BadRequest caused by empty title" in {
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
        .run(Request(method = Method.POST, uri = uri"/v1/contents/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Bad Request")
            assert(maybeError.status === 400)
            assert(maybeError.detail === "title required.")
            assert(maybeError.instance === s"/v1/contents/")
          }
        }
        .unsafeRunSync()
    }

    "return 400 BadRequest caused by empty rawContent" in {
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
        .run(Request(method = Method.POST, uri = uri"/v1/contents/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Bad Request")
            assert(maybeError.status === 400)
            assert(maybeError.detail === "rawContent required.")
            assert(maybeError.instance === s"/v1/contents/")
          }
        }
        .unsafeRunSync()
    }

    "return 400 BadRequest caused by empty htmlContent" in {
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
        .run(Request(method = Method.POST, uri = uri"/v1/contents/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Bad Request")
            assert(maybeError.status === 400)
            assert(maybeError.detail === "htmlContent required.")
            assert(maybeError.instance === s"/v1/contents/")
          }
        }
        .unsafeRunSync()
    }

    "reject caused by expired token" in {
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
        .run(
          Request(method = Method.POST, uri = uri"/v1/contents/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + expiredToken)), entity = entity)
        )
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "reject POST endpoint caused by the authorization header is empty" in {
      client
        .run(Request(method = Method.POST, uri = uri"/v1/contents/"))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "reject POST endpoint caused by invalid token" in {
      val entity = EntityEncoder[IO, String].toEntity("")
      client
        .run(
          Request(
            method = Method.POST,
            uri = uri"/v1/contents/",
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + "invalid Token")),
            entity = entity
          )
        )
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "return user not found" in {
      val entity = EntityEncoder[IO, String].toEntity("")
      client
        .run(
          Request(
            method = Method.POST,
            uri = uri"/v1/contents/",
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + nonExistsUserToken)),
            entity = entity
          )
        )
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "reject with bad request (wrong JSON format)" in {
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
        .run(Request(method = Method.POST, uri = uri"/v1/contents/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Bad Request")
            assert(maybeError.status === 400)
            assert(maybeError.detail === "Invalid JSON format or missing required field. Please see API document.")
            assert(maybeError.instance === s"/v1/contents/")
          }
        }
        .unsafeRunSync()
    }

    "reject with bad request (lack of JSON key)" in {
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
        .run(Request(method = Method.POST, uri = uri"/v1/contents/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Bad Request")
            assert(maybeError.status === 400)
            assert(maybeError.detail === "Invalid JSON format or missing required field. Please see API document.")
            assert(maybeError.instance === s"/v1/contents/")
          }
        }
        .unsafeRunSync()
    }

    "return specific content" in {
      val now = Instant.now.getEpochSecond

      // NOTE: create content and related data for test
      contentService
        .createOrUpdate(
          validAuthor.name,
          ContentRequestModel(
            contentType = "article",
            path = ContentPath("/test/content/route/spec/2"),
            title = "this is a ContentRouteSpec2 title",
            rawContent = "this is a raw ContentRouteSpec2",
            htmlContent = "<p>this is a html ContentRouteSpec2<p>",
            robotsAttributes = Attributes("noarchive, noimageindex"),
            tags = List(Tag(name = TagName("ContentRoute"), path = TagPath("ContentRoute-path"))),
            externalResources = List(),
            publishedAt = 1644075206
          )
        )
        .unsafeRunSync()

      client
        .run(
          Request(
            method = Method.GET,
            uri = uri"/v1/contents/test/content/route/spec/2"
          )
        )
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))

            val maybeContent = unsafeDecode[ContentDetailResponseModel](response)
            assert(maybeContent.authorName === validAuthor.displayName.toString().toLowerCase())
            assert(maybeContent.content === "<p>this is a html ContentRouteSpec2<p>")
            assert(maybeContent.description === "this is a html ContentRouteSpec2")
            assert(maybeContent.externalResources.isEmpty)
            assert(maybeContent.length === "this is a html ContentRouteSpec2".replaceAll(" ", "").size)
            assert(maybeContent.publishedAt === 1644075206)
            assert(maybeContent.robotsAttributes === "noarchive, noimageindex")
            assert(maybeContent.tags.size === 1)
            assert(maybeContent.tags.head.name === "ContentRoute")
            assert(maybeContent.title === "this is a ContentRouteSpec2 title")
            assert(maybeContent.updatedAt >= now)
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

    "return 404 with non-trailing slash" in {
      client
        .run(
          Request(
            method = Method.GET,
            uri = uri"/v1/contents/this/is/a/404"
          )
        )
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail === "Not Found")
            assert(maybeError.instance === "/v1/contents/this/is/a/404")
          }
        }
        .unsafeRunSync()
    }

    "return 404 with trailing slash" in {
      client
        .run(
          Request(
            method = Method.GET,
            uri = uri"/v1/contents/this/is/a/404/"
          )
        )
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail === "Not Found")
            assert(maybeError.instance === "/v1/contents/this/is/a/404/")
          }
        }
        .unsafeRunSync()
    }

    "return method not allowed" in {
      client
        .run(
          Request(
            method = Method.PATCH,
            uri = uri"/v1/contents/this/is/a/404/",
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === MethodNotAllowed)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

  }

}
