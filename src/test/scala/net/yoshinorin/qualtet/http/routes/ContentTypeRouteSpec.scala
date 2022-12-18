package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import net.yoshinorin.qualtet.fixture.Fixture.router
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.ContentTypeRouteSpec
class ContentTypeRouteSpec extends AnyWordSpec {

  val client: Client[IO] = Client.fromHttpApp(router.routes)

  "ContentTypeRoute" should {

    "be return content-types" in {
      client
        .run(Request(method = Method.GET, uri = uri"/content-types/"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
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
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("article"))
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
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("page"))
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
          }
        }
        .unsafeRunSync()
    }
  }

}
