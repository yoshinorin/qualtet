package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.Modules._
import net.yoshinorin.qualtet.fixture.Fixture
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.SearchRouteSpec
class SearchRouteSpec extends AnyWordSpec {

  val requestContents: List[RequestContent] = {
    (0 until 49).toList
      .map(_.toString())
      .map(i =>
        RequestContent(
          contentType = "article",
          path = Path(s"/test/searchRoute-${i}"),
          title = s"this is a searchRoute title ${i}",
          rawContent = s"this is a searchRoute raw content ${i}",
          htmlContent = s"this is a searchRoute html content ${i}",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          tags = List(s"searchRoute${i}"),
          externalResources = List()
        )
      )
  }
  requestContents :+ RequestContent(
    contentType = "article",
    path = Path(s"/test/searchRouteLast"),
    title = s"this is a searchRoute titleLast",
    rawContent = s"this is a searchRoute raw contentLast",
    htmlContent = s"this is a searchRoute html contentLast",
    robotsAttributes = Attributes("noarchive, noimageindex"),
    tags = List(s"searchServiceLast"),
    externalResources = List()
  )

  // NOTE: create content and related data for test
  requestContents.foreach { rc => contentService.createContentFromRequest(AuthorName(Fixture.author.name.value), rc).unsafeRunSync() }

  val client: Client[IO] = Client.fromHttpApp(Fixture.router.routes)

  "SearchRoute" should {
    "be return search result" in {
      client
        .run(Request(method = Method.GET, uri = uri"/search/?q=route"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            // TODO: assert json
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("contents"))
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("searchRoute"))
          }
        }
        .unsafeRunSync()
    }

    "be return BadRequest without query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/search/"))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
          }
        }
        .unsafeRunSync()
    }

    "be return BadRequest with query param short value" in {
      client
        .run(Request(method = Method.GET, uri = uri"/search/?q=abc"))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
          }
        }
        .unsafeRunSync()
    }

    "be return BadRequest with invalid query param" in {
      client
        .run(Request(method = Method.GET, uri = uri"/search/?invalid=abcd"))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
          }
        }
        .unsafeRunSync()
    }

    "be return BadRequest with query param contains invalid values" in {
      client
        .run(Request(method = Method.GET, uri = uri"/search/?q=a.b.c"))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
          }
        }
        .unsafeRunSync()
    }

    "be return BadRequest with too many query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/search/?q=abcd&q=abcd&q=abcd&q=abcd"))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
          }
        }
        .unsafeRunSync()
    }
  }
}