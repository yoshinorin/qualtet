package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import org.typelevel.ci.*
import net.yoshinorin.qualtet.auth.RequestToken
import net.yoshinorin.qualtet.domains.articles.ResponseArticleWithCount
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.domains.tags.{ResponseTag, TagId}
import net.yoshinorin.qualtet.message.ProblemDetails
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.Modules.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.v1.TagRouteSpec
class TagRouteV1Spec extends AnyWordSpec with BeforeAndAfterAll {

  val requestContents = makeRequestContents(10, "tagRoute")
  // NOTE: create content and related data for test
  createContents(requestContents)

  /* TODO: `BeforeAndAfterAll` seems doesn't work on CI this test class.
  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContents(requestContents)
  }
   */

  val validAuthor: ResponseAuthor = authorService.findByName(author.name).unsafeRunSync().get
  val validToken: String = authService.generateToken(RequestToken(validAuthor.id, "pass")).unsafeRunSync().token
  val tagRouteV1 = new TagRoute(authProvider, tagService, articleService)
  val client: Client[IO] = Client.fromHttpApp(makeRouter(tagRouteV1 = tagRouteV1).routes.orNotFound)

  "TagRoute" should {

    val t: Seq[ResponseTag] = tagService.getAll.unsafeRunSync().filter(t => t.name.value.startsWith("tagRouteTag"))

    "be return tags" in {
      val expectJson =
        s"""
          |{
          |  "id" : "${t(0).id.value}",
          |  "name" : "${t(0).name.value}",
          |  "count" : 1
          |},
          |{
          |  "id" : "${t(1).id.value}",
          |  "name" : "${t(1).name.value}",
          |  "count" : 1
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      client
        .run(Request(method = Method.GET, uri = uri"/v1/tags/"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains(expectJson))
          }
        }
        .unsafeRunSync()
    }

    "be return specific tag" in {
      client
        .run(Request(method = Method.GET, uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/tags/${t(0).name.value}"))))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))

            val maybeArticles = unsafeDecode[ResponseArticleWithCount](response)
            assert(maybeArticles.count === 1)
            assert(maybeArticles.articles.head.path.toString().startsWith("/test/tagRoute-0"))
          }
        }
        .unsafeRunSync()

      client
        .run(Request(method = Method.GET, uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/tags/${t(1).name.value}"))))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))

            val maybeArticles = unsafeDecode[ResponseArticleWithCount](response)
            assert(maybeArticles.count === 1)
            assert(maybeArticles.articles.head.path.toString().startsWith("/test/tagRoute-1"))
          }
        }
        .unsafeRunSync()
    }

    /*
     TODO: Why do these tests are returns 404?
    "be return specific tag contents with query params" in {
      client
        // .run(Request(method = Method.GET, uri = new Uri().withPath(s"/tags/${t(1).name.value}?page=1&limit=10")))
        .run(Request(method = Method.GET, uri = host.withPath("/tags/tagRoute-0?page=1&limit=10")))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("/test/tagRoute-0"))
          }
        }
        .unsafeRunSync()
    }

    "be return 10 specific tag contents with query params" in {
      client
        // .run(Request(method = Method.GET, uri = new Uri().withPath(s"/tags/${t(0).name.value}?page=1&limit=50")))
        .run(Request(method = Method.GET, uri = host.withPath(s"/tags/tagRoute-0?page=1&limit=50")))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("/test/tagRoute-0"))
          }
        }
        .unsafeRunSync()
    }
     */

    "be return 404" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/tags/not-exists"))
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail.startsWith("articles not found"))
            assert(maybeError.instance === "/v1/tags/not-exists")
          }
        }
        .unsafeRunSync()
    }

    "be delete a tag" in {
      val tag = tagService.findByName(t(4).name).unsafeRunSync().get

      // 204 (first time)
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/tags/${tag.id.value}")),
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
      assert(tagService.findByName(t(4).name).unsafeRunSync().isEmpty)

      // 404 (second time)
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/tags/${tag.id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail.startsWith("tag not found: "))
            assert(maybeError.instance === s"/v1/tags/${tag.id.value}")
          }
        }
        .unsafeRunSync()
    }

    "be return 404 DELETE endopoint" in {
      val id = TagId(generateUlid())
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/tags/${id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail.startsWith("tag not found: "))
            assert(maybeError.instance === s"/v1/tags/${id.value}")
          }
        }
        .unsafeRunSync()
    }

    "be reject DELETE endpoint caused by invalid token" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/v1/tags/reject", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + "invalid token"))))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "be return Method Not Allowed" in {
      val tag = tagService.findByName(t(2).name).unsafeRunSync().get
      client
        .run(
          Request(
            method = Method.PATCH,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/tags/${tag.id.value}")),
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
