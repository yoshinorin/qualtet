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
  val request: Request[IO] = Request(method = Method.GET, uri = uri"/status")
  val client: Client[IO] = Client.fromHttpApp(Fixture.httpApp)

  "ApiStatusRoute" should {

    "return operational JSON" in {
      val response = client.expect[String](request).unsafeRunSync()
      assert(response.replaceAll("\n", "").replaceAll(" ", "") === "{\"status\":\"operational\"}")

      //assert(r.status === Ok)
      //assert(r.contentType.get === `Content-Type`(MediaType.application.json))

      }
    }
  }

