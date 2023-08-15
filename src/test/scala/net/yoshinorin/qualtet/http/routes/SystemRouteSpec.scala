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

// testOnly net.yoshinorin.qualtet.http.routes.SystemRouteSpec
class SystemRouteSpec extends AnyWordSpec {

  val systemRoute: SystemRoute = new SystemRoute()
  val router = Fixture.makeRouter(systemRoute = systemRoute)

  val request: Request[IO] = Request(method = Method.GET, uri = uri"/status")
  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "SystemRoute" should {

    "health return 200" in {
      client
        .run(Request(method = Method.GET, uri = uri"/system/health"))
        .use { response =>
          IO {
            assert(response.status === Ok)
          }
        }
        .unsafeRunSync()
    }
  }

  "be return Method Not Allowed" in {
    client
      .run(Request(method = Method.DELETE, uri = uri"/system"))
      .use { response =>
        IO {
          assert(response.status === MethodNotAllowed)
        }
      }
      .unsafeRunSync()
  }
}