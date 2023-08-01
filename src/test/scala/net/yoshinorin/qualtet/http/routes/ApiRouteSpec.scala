package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.fixture.Fixture

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.ApiRouteSpec
class ApiRouteSpec extends AnyWordSpec {

  val apiStatusRoute: ApiStatusRoute = new ApiStatusRoute()
  val router = Fixture.makeRouter(apiStatusRoute = apiStatusRoute)

  val request: Request[IO] = Request(method = Method.GET, uri = uri"/status")
  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "ApiStatusRoute" should {

    "return operational JSON" in {
      client
        .run(Request(method = Method.GET, uri = uri"/status"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "") === "{\"status\":\"operational\"}")
          }
        }
        .unsafeRunSync()
    }
  }

  "be return Method Not Allowed" in {
    client
      .run(Request(method = Method.DELETE, uri = uri"/status"))
      .use { response =>
        IO {
          assert(response.status === MethodNotAllowed)
        }
      }
      .unsafeRunSync()
  }
}
