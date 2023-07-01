package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.Modules.*
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.SearchRouteSpec
class SearchRouteSpec extends AnyWordSpec with BeforeAndAfterAll {

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
      ) :+ RequestContent(
      contentType = "article",
      path = Path(s"/test/searchServiceLast"),
      title = s"this is a searchService titleLast",
      rawContent = s"this is a searchService raw contentLast",
      htmlContent = s"this is a searchService html contentLast",
      robotsAttributes = Attributes("noarchive, noimageindex"),
      tags = List(s"searchServiceLast"),
      externalResources = List()
    )
  }

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContents(requestContents)
  }

  val client: Client[IO] = Client.fromHttpApp(router.routes)

  "SearchRoute" should {
    "be return search result" in {
      client
        .run(Request(method = Method.GET, uri = uri"/search/?q=searchRoute"))
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

    "be return UnprocessableEntity without query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/search/"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
          }
        }
        .unsafeRunSync()
    }

    "be return UnprocessableEntity with query param short value" in {
      client
        .run(Request(method = Method.GET, uri = uri"/search/?q=abc"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
          }
        }
        .unsafeRunSync()
    }

    "be return UnprocessableEntity with invalid query param" in {
      client
        .run(Request(method = Method.GET, uri = uri"/search/?invalid=abcd"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
          }
        }
        .unsafeRunSync()
    }

    "be return UnprocessableEntity with query param contains invalid values" in {
      client
        .run(Request(method = Method.GET, uri = uri"/search/?q=a.b.c"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
          }
        }
        .unsafeRunSync()
    }

    "be return UnprocessableEntity with too many query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/search/?q=abcd&q=abcd&q=abcd&q=abcd"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
          }
        }
        .unsafeRunSync()
    }
  }
}
