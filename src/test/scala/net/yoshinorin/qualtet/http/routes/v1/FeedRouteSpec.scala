package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import net.yoshinorin.qualtet.domains.feeds.FeedResponseModel
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.v1.FeedRouteSpec
class FeedRouteSpec extends AnyWordSpec with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContentRequestModels(2, "feedsRoute").unsafeCreateConternt()
  }

  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "FeedRoute" should {
    "return feeds" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/feeds/index"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            // TODO: assert response contents count

            val maybeFeeds = unsafeDecode[Seq[FeedResponseModel]](response)
            assert(maybeFeeds.size === 5)
          }
        }
        .unsafeRunSync()
    }

    "return NoContent" in {
      client
        .run(Request(method = Method.OPTIONS, uri = uri"/v1/feeds"))
        .use { response =>
          IO {
            assert(response.status === NoContent)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "return Method Not Allowed" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/v1/feeds/index"))
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
