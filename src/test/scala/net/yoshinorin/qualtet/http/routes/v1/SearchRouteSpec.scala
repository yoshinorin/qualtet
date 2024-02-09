package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.search.ResponseSearchWithCount
import net.yoshinorin.qualtet.message.ProblemDetails
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.v1.SearchRouteSpec
class SearchRouteV1Spec extends AnyWordSpec with BeforeAndAfterAll {

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

  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "SearchRoute" should {
    "be return search result" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/search/?q=searchRoute"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            // TODO: assert json
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("contents"))
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("searchRoute"))

            val maybeSearchResult = unsafeDecode[ResponseSearchWithCount](response)
            assert(maybeSearchResult.count >= 49) // FIXME: Get number of articles for search result with service class and use it for assertion.
            assert(maybeSearchResult.contents.size === 30)
            assert(maybeSearchResult.contents.head.path.toString().startsWith("/test/searchRoute-"))
            assert(maybeSearchResult.contents(29).path.toString().startsWith("/test/searchRoute-"))
          }
        }
        .unsafeRunSync()
    }

    "be return UnprocessableEntity without query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/search/"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ProblemDetails](response)
            assert(maybeError.title === "Unprocessable Entity")
            assert(maybeError.status === 422)
            assert(maybeError.detail === "SEARCH_QUERY_REQUIRED")
            assert(maybeError.instance === "/v1/search/")
          }
        }
        .unsafeRunSync()
    }

    "be return UnprocessableEntity with query param short value" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/search/?q=abc"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ProblemDetails](response)
            assert(maybeError.title === "Unprocessable Entity")
            assert(maybeError.status === 422)
            assert(maybeError.detail === "SEARCH_CHAR_LENGTH_TOO_SHORT")
            assert(maybeError.instance === "/v1/search/?q=abc")
          }
        }
        .unsafeRunSync()
    }

    "be return UnprocessableEntity with invalid query param" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/search/?invalid=abcd"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ProblemDetails](response)
            assert(maybeError.title === "Unprocessable Entity")
            assert(maybeError.status === 422)
            assert(maybeError.detail === "SEARCH_QUERY_REQUIRED")
            assert(maybeError.instance === "/v1/search/?invalid=abcd")
          }
        }
        .unsafeRunSync()
    }

    "be return UnprocessableEntity with query param contains invalid values" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/search/?q=a.b.c"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ProblemDetails](response)
            assert(maybeError.title === "Unprocessable Entity")
            assert(maybeError.status === 422)
            assert(maybeError.detail === "INVALID_CHARS_INCLUDED")
            assert(maybeError.instance === "/v1/search/?q=a.b.c")
          }
        }
        .unsafeRunSync()
    }

    "be return UnprocessableEntity with too many query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/search/?q=abcd&q=abcd&q=abcd&q=abcd"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ProblemDetails](response)
            assert(maybeError.title === "Unprocessable Entity")
            assert(maybeError.status === 422)
            assert(maybeError.detail === "TOO_MANY_SEARCH_WORDS")
            assert(maybeError.instance === "/v1/search/?q=abcd&q=abcd&q=abcd&q=abcd")
          }
        }
        .unsafeRunSync()
    }
  }
}
