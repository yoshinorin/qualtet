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
import net.yoshinorin.qualtet.http.ResponseProblemDetails
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.v1.SearchRouteV1Spec
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
    "return search result" in {
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

    "return UnprocessableEntity without query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/search/"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Unprocessable Entity")
            assert(maybeError.status === 422)
            assert(maybeError.detail === "Invalid search conditions. Please see error details.")
            assert(maybeError.instance === "/v1/search/")

            val err = maybeError.errors.get.head
            assert(err.code === "SEARCH_QUERY_REQUIRED")
            assert(err.message === "Search query required.")
          }
        }
        .unsafeRunSync()
    }

    "return UnprocessableEntity with query param short value" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/search/?q=abc"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Unprocessable Entity")
            assert(maybeError.status === 422)
            assert(maybeError.detail === "Invalid search conditions. Please see error details.")
            assert(maybeError.instance === "/v1/search/?q=abc")

            val err = maybeError.errors.get.head
            assert(err.code === "SEARCH_CHAR_LENGTH_TOO_SHORT")
            assert(err.message === "abc is too short. You must be more than 4 chars in one word.")
          }
        }
        .unsafeRunSync()
    }

    "return UnprocessableEntity with invalid query param" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/search/?invalid=abcd"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Unprocessable Entity")
            assert(maybeError.status === 422)
            assert(maybeError.detail === "Invalid search conditions. Please see error details.")
            assert(maybeError.instance === "/v1/search/?invalid=abcd")

            val err = maybeError.errors.get.head
            assert(err.code === "SEARCH_QUERY_REQUIRED")
            assert(err.message === "Search query required.")
          }
        }
        .unsafeRunSync()
    }

    "return UnprocessableEntity with query param contains invalid values" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/search/?q=a.b.c"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Unprocessable Entity")
            assert(maybeError.status === 422)
            assert(maybeError.detail === "Invalid search conditions. Please see error details.")
            assert(maybeError.instance === "/v1/search/?q=a.b.c")

            val err = maybeError.errors.get.head
            assert(err.code === "INVALID_CHARS_INCLUDED")
            assert(err.message === "Contains unusable chars in a.b.c")
          }
        }
        .unsafeRunSync()
    }

    "return UnprocessableEntity with too many query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/search/?q=abcd&q=abcd&q=abcd&q=abcd"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Unprocessable Entity")
            assert(maybeError.status === 422)
            assert(maybeError.detail === "Invalid search conditions. Please see error details.")
            assert(maybeError.instance === "/v1/search/?q=abcd&q=abcd&q=abcd&q=abcd")

            val err = maybeError.errors.get.head
            assert(err.code === "TOO_MANY_SEARCH_WORDS")
            assert(err.message === "Search words must be less than 3. You specified 4.")
          }
        }
        .unsafeRunSync()
    }

    "return UnprocessableEntity with accumulated errors" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/search/?q=a.b.c&q=x&q=z.zzzzzzzzzzzzzzzzzzz&q=abcd&q=.y"))
        .use { response =>
          IO {
            assert(response.status === UnprocessableEntity)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Unprocessable Entity")
            assert(maybeError.status === 422)
            assert(maybeError.detail === "Invalid search conditions. Please see error details.")
            assert(maybeError.instance === "/v1/search/?q=a.b.c&q=x&q=z.zzzzzzzzzzzzzzzzzzz&q=abcd&q=.y")

            val err = maybeError.errors.get
            assert(err.filter(x => x.code.contains("TOO_MANY_SEARCH_WORDS")).size === 1)
            assert(err.filter(x => x.code.contains("SEARCH_CHAR_LENGTH_TOO_SHORT")).size === 2)
            assert(err.filter(x => x.code.contains("INVALID_CHARS_INCLUDED")).size === 3)
            assert(err.filter(x => x.message.contains("Search words must be less than 3. You specified 5.")).size === 1)
            assert(err.filter(x => x.message.contains("x is too short. You must be more than 4 chars in one word.")).size === 1)
            assert(err.filter(x => x.message.contains(".y is too short. You must be more than 4 chars in one word.")).size === 1)
            assert(err.filter(x => x.message.contains("Contains unusable chars in a.b.c")).size === 1)
            assert(err.filter(x => x.message.contains("Contains unusable chars in z.zzzzzzzzzzzzzzzzzzz")).size === 1)
            assert(err.filter(x => x.message.contains("Contains unusable chars in .y")).size === 1)
          }

        }
        .unsafeRunSync()
    }
  }
}
