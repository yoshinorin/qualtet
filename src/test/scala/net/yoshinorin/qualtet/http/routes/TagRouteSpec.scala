package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import org.typelevel.ci._
import net.yoshinorin.qualtet.auth.RequestToken
import net.yoshinorin.qualtet.domains.authors.{AuthorName, ResponseAuthor}
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.tags.{TagId, ResponseTag}
import net.yoshinorin.qualtet.fixture.Fixture._
import net.yoshinorin.qualtet.Modules._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.TagRouteSpec
class TagRouteSpec extends AnyWordSpec with BeforeAndAfterAll {

  val requestContents = makeRequestContents(5, "tagRoute")

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContents(requestContents)
  }

  val validAuthor: ResponseAuthor = authorService.findByName(author.name).unsafeRunSync().get
  val validToken: String = authService.generateToken(RequestToken(validAuthor.id, "pass")).unsafeRunSync().token
  val tagRoute = new TagRoute(tagService, articleService)
  val client: Client[IO] = Client.fromHttpApp(makeRouter(tagRoute = tagRoute).routes)

  "TagRoute" should {

    val t: Seq[ResponseTag] = tagService.getAll.unsafeRunSync().filter(t => t.name.value.contains("tagRouteTag"))

    "be return tags" in {
      val expectJson =
        s"""
          |{
          |  "id" : "${t(0).id.value}",
          |  "name" : "${t(0).name.value}"
          |},
          |{
          |  "id" : "${t(1).id.value}",
          |  "name" : "${t(1).name.value}"
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      client
        .run(Request(method = Method.GET, uri = uri"/tags/"))
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
        .run(Request(method = Method.GET, uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/tags/${t(0).name.value}"))))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("/test/tagRoute-0"))
          }
        }
        .unsafeRunSync()

      client
        .run(Request(method = Method.GET, uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/tags/${t(1).name.value}"))))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("/test/tagRoute-1"))
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
        .run(Request(method = Method.GET, uri = uri"/tags/not-exists"))
        .use { response =>
          IO {
            assert(response.status === NotFound)
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
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/tags/${tag.id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === NoContent)
          }
        }
        .unsafeRunSync()
      assert(tagService.findByName(t(4).name).unsafeRunSync().isEmpty)

      // 404 (second time)
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/tags/${tag.id.value}")),
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
      val id = TagId(generateUlid())
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/tags/${id.value}")),
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

    "be reject DELETE endpoint caused by invalid token" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/tags/reject", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + "invalid token"))))
        .use { response =>
          IO {
            // TODO: assert(response.status === Unauthorized)
          }
        }
      // TODO: .unsafeRunSync()
    }

    "be return Method Not Allowed" in {
      val tag = tagService.findByName(t(2).name).unsafeRunSync().get
      client
        .run(
          Request(
            method = Method.PATCH,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/tags/${tag.id.value}")),
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
