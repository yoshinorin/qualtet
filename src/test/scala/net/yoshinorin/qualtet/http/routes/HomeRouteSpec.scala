package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import net.yoshinorin.qualtet.fixture.Fixture.router
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.HomeRouteSpec
class HomeRouteSpec extends AnyWordSpec {

  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "HomeRoute" should {

    "hello Qualtet!!" in {
      client
        .run(Request(method = Method.GET, uri = uri"/"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get.mediaType === MediaType.text.plain)
            assert(response.contentType.get.charset.get === Charset.`UTF-8`)
            assert(response.as[String].unsafeRunSync().contains("Hello Qualtet!!"))
          }
        }
        .unsafeRunSync()
    }

    "return Not Found" in {
      client
        .run(Request(method = Method.GET, uri = uri"/not-found"))
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get.charset.get === Charset.`UTF-8`)
            assert(response.as[String].unsafeRunSync().contains("Not found"))
          }
        }
        .unsafeRunSync()
    }
  }

}
