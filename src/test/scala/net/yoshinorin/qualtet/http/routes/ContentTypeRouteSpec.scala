package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import net.yoshinorin.qualtet.domains.contentTypes.ContentType
import net.yoshinorin.qualtet.message.Message
import net.yoshinorin.qualtet.fixture.Fixture.{router, unsafeDecode}
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.ContentTypeRouteSpec
class ContentTypeRouteSpec extends AnyWordSpec {

  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "ContentTypeRoute" should {

    "be return content-types" in {
      client
        .run(Request(method = Method.GET, uri = uri"/content-types/"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))

            val maybeContentTypes = unsafeDecode[Seq[ContentType]](response)
            assert(maybeContentTypes.size === 2)
          }
        }
        .unsafeRunSync()
    }

    "be return content-type:articles" in {
      client
        .run(Request(method = Method.GET, uri = uri"/content-types/article"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))

            val maybeContentType = unsafeDecode[ContentType](response)
            // assert(maybeContentType.id === "TODO") // TODO: assert id is ULID
            assert(maybeContentType.name === "article")
          }
        }
        .unsafeRunSync()
    }

    "be return content-type:page" in {
      client
        .run(Request(method = Method.GET, uri = uri"/content-types/page"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))

            val maybeContentType = unsafeDecode[ContentType](response)
            // assert(maybeContentType.id === "TODO") // TODO: assert id is ULID
            assert(maybeContentType.name === "page")
          }
        }
        .unsafeRunSync()
    }

    "be return content-type:not-exists" in {
      client
        .run(Request(method = Method.GET, uri = uri"/content-types/not-exists"))
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))

            val maybeError = unsafeDecode[Message](response)
            assert(maybeError.message === "Not Found")
          }
        }
        .unsafeRunSync()
    }

    "be return Method Not Allowed" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/content-types"))
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
