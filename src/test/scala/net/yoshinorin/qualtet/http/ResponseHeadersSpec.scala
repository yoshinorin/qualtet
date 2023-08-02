package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.typelevel.ci._
import net.yoshinorin.qualtet.HttpAppBuilder
import net.yoshinorin.qualtet.fixture.Fixture.router
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.ResponseHeadersSpec
class ResponseHeadersSpec extends AnyWordSpec {

  val httpApp = new HttpAppBuilder(router.routes.orNotFound).build
  val client: Client[IO] = Client.fromHttpApp(httpApp)

  "Response" should {

    "be contains specific headers" in {
      client
        .run(Request(method = Method.GET, uri = uri"/"))
        .use { response =>
          IO {
            val reqId = response.headers.get(ci"X-Request-Id")
            assert(reqId.isDefined)
            val resTime = response.headers.get(ci"X-Response-Time")
            assert(resTime.isDefined)
          }
        }
        .unsafeRunSync()
    }

    "be contains x-request-id header and its value is same with request value (UUID4)" in {
      val uuid4String = "76de4439-1521-4bf4-86bf-ccd38afc416a"
      client
        .run(Request(method = Method.GET, uri = uri"/", headers = Headers(Header.Raw(ci"X-Request-Id", uuid4String))))
        .use { response =>
          IO {
            val reqId = response.headers.get(ci"X-Request-Id")
            assert(reqId.isDefined)
            assert(reqId.head.head.value === uuid4String)
          }
        }
        .unsafeRunSync()
    }

    "be contains x-request-id header and its value is same with request value (NOT a UUID)" in {
      val notUuidString = "not uuid string"
      client
        .run(Request(method = Method.GET, uri = uri"/", headers = Headers(Header.Raw(ci"X-Request-Id", notUuidString))))
        .use { response =>
          IO {
            val reqId = response.headers.get(ci"X-Request-Id")
            assert(reqId.isDefined)
            assert(reqId.head.head.value === notUuidString)
          }
        }
        .unsafeRunSync()
    }
  }

}
