package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.fixture.Fixture

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.ApiRouteSpec
class ApiRouteSpec extends AnyWordSpec {

  val apiStatusRoute: ApiStatusRoute = new ApiStatusRoute()
  val router = Fixture.createRouter(apiStatusRoute = apiStatusRoute)

  val request: Request[IO] = Request(method = Method.GET, uri = uri"/status")
  val client: Client[IO] = Client.fromHttpApp(router.routes)

  "ApiStatusRoute" should {

    "return operational JSON" in {
        client.run(request).use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "") === "{\"status\":\"operational\"}")
          }
        }.unsafeRunSync()
      }
    }
  }
