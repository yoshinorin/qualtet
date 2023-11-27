package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import net.yoshinorin.qualtet.domains.feeds.ResponseFeed
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.FeedRouteSpec
class FeedRouteSpec extends AnyWordSpec with BeforeAndAfterAll {

  val requestContents = makeRequestContents(2, "feedsRoute")

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContents(requestContents)
  }

  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "FeedRoute" should {
    "be return feeds" in {
      client
        .run(Request(method = Method.GET, uri = uri"/feeds/index"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            // TODO: assert response contents count

            val maybeFeeds = unsafeDecode[Seq[ResponseFeed]](response)
            assert(maybeFeeds.size === 5)
          }
        }
        .unsafeRunSync()
    }

    "be return Method Not Allowed" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/feeds/index"))
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
