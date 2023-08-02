/*
package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.typelevel.ci._
import net.yoshinorin.qualtet.fixture.Fixture.router
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.ResponseHeadersSpec
class ResponseHeadersSpec extends AnyWordSpec {

  // TODO: build from `BootStrap.buildHttpApp`
  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "Response" should {

    "be contains specific headers" in {
      client
        .run(Request(method = Method.GET, uri = uri"/"))
        .use { response =>
          IO {
            println(response.headers)
            assert(response.headers.get(ci"X-Request-Id").isInstanceOf[String])
            assert(response.headers.get(ci"X-Response-Time").isInstanceOf[Int])
          }
        }
        .unsafeRunSync()
    }
  }

}
*/
