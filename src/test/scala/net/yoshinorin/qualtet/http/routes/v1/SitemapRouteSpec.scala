package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import net.yoshinorin.qualtet.domains.sitemaps.Url
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.v1.SitemapRouteSpec
class SitemapRouteV1Spec extends AnyWordSpec with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    val requestContents = makeRequestContents(2, "sitemapRoute")
    createContents(requestContents)
  }

  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "SitemapRoute" should {
    "return json for sitemap.xml" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/sitemaps"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceNewlineAndSpace.contains("loc"))

            val maybeSitemap = unsafeDecode[Seq[Url]](response)
            assert(maybeSitemap.size >= 2) // FIXME
          }
        }
        .unsafeRunSync()
    }

    "return Method Not Allowed" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/v1/sitemaps"))
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
