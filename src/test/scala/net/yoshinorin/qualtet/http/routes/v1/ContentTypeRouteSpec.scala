package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import net.yoshinorin.qualtet.domains.contentTypes.ContentType
import net.yoshinorin.qualtet.http.errors.ResponseProblemDetails
import net.yoshinorin.qualtet.fixture.Fixture.{router, unsafeDecode}
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.v1.ContentTypeRouteSpec
class ContentTypeRouteSpec extends AnyWordSpec {

  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "ContentTypeRoute" should {

    "return content-types" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/content-types/"))
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

    "return content-type:articles" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/content-types/article"))
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

    "return content-type:page" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/content-types/page"))
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

    "return content-type:not-exists" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/content-types/not-exists"))
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail === "Not Found")
            assert(maybeError.instance === "/v1/content-types/not-exists")
          }
        }
        .unsafeRunSync()
    }

    "return Method Not Allowed" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/v1/content-types"))
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
