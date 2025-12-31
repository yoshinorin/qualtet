package net.yoshinorin.qualtet.http.routes.v1

import net.yoshinorin.qualtet.fixture.unsafe
import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import org.typelevel.ci.*
import net.yoshinorin.qualtet.auth.RequestToken
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.domains.series.{Series, SeriesId, SeriesName, SeriesPath, SeriesRequestModel}
import net.yoshinorin.qualtet.http.errors.ResponseProblemDetails
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.fixture.Fixture.log4catsLogger
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.v1.SeriesRouteSpec
class SeriesRouteSpec extends AnyWordSpec with BeforeAndAfterAll {

  val requestSeries: List[SeriesRequestModel] = List(
    SeriesRequestModel(
      title = "Series Route Spec",
      name = SeriesName("seriesroute-series-name"),
      path = SeriesPath("seriesroute-series-path").unsafe,
      description = Some("Series Route Spec Description1")
    ).unsafe,
    SeriesRequestModel(
      title = "Series Route Spec2",
      name = SeriesName("seriesroute-series2-name"),
      path = SeriesPath("seriesroute-series2-path").unsafe,
      description = Some("Series Route Spec Description2")
    ).unsafe,
    SeriesRequestModel(
      title = "Series Route Spec3",
      name = SeriesName("seriesroute-series3-name"),
      path = SeriesPath("seriesroute-series3-path").unsafe,
      description = Some("Series Route Spec Description2")
    ).unsafe
  )

  requestSeries.unsafeCreateSeries()
  createContentRequestModels(5, "SeriesRoute", Option(requestSeries.head.name)).unsafeCreateConternt()

  /* TODO: `BeforeAndAfterAll` seems doesn't work on CI this test class.
  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    requestSeries.unsafeCreateSeries()
    requestContents.unsafeCreateConternt
  }
   */

  val validAuthor: AuthorResponseModel = authorService.findByName(author.name).unsafeRunSync().get
  val validToken: String = authService.generateToken(RequestToken(validAuthor.id, "pass")).flatMap(IO.fromEither).unsafeRunSync().token
  val seriesRouteV1 = new SeriesRoute(authProvider, seriesService)
  val client: Client[IO] = Client.fromHttpApp(makeRouter(seriesRouteV1 = seriesRouteV1).routes.orNotFound)

  "SeriesRoute" should {

    val s1: Series = seriesService.findByName(requestSeries.head.name).unsafeRunSync().get
    val s2: Series = seriesService.findByName(requestSeries(1).name).unsafeRunSync().get
    val s3: Series = seriesService.findByName(requestSeries(2).name).unsafeRunSync().get

    "create a series" in {
      val json =
        """
          |{
          |  "name": "example series",
          |  "path": "example-series-route-path",
          |  "title": "example series title",
          |  "description": "example series description"
          |}
        """.stripMargin
      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/v1/series/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === Created)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))

            val maybeSeries = unsafeDecode[Series](response)
            assert(maybeSeries.name === "example series")
            assert(maybeSeries.path === "/example-series-route-path")
            // assert(maybeSeries.id === "TODO")  // TODO: assert id is ULID
            assert(maybeSeries.description.get === "example series description")
            assert(maybeSeries.title === "example series title")
          }
        }
        .unsafeRunSync()
    }

    "return series" in {
      val expectPartialJson =
        s"""
          |{
          |  "id" : "${s1.id.value}",
          |  "name" : "${s1.name.value}",
          |  "path" : "${s1.path.value}",
          |  "title" : "${s1.title}",
          |  "description" : "${s1.description.get}"
          |}
      """.stripMargin.replaceNewlineAndSpace

      val expectPartialJson2 =
        s"""
          |{
          |  "id" : "${s2.id.value}",
          |  "name" : "${s2.name.value}",
          |  "path" : "${s2.path.value}",
          |  "title" : "${s2.title}",
          |  "description" : "${s2.description.get}"
          |}
      """.stripMargin.replaceNewlineAndSpace

      client
        .run(Request(method = Method.GET, uri = uri"/v1/series/"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceNewlineAndSpace.contains(expectPartialJson))
            assert(response.as[String].unsafeRunSync().replaceNewlineAndSpace.contains(expectPartialJson2))
          }
        }
        .unsafeRunSync()
    }

    "return specific series" in {
      client
        .run(Request(method = Method.GET, uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/series${s1.path.value}"))))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceNewlineAndSpace.contains("seriesroute-series"))

            val maybeSeries = unsafeDecode[Series](response)
            assert(maybeSeries.name === "seriesroute-series-name")
            assert(maybeSeries.path === "/seriesroute-series-path")
            // assert(maybeSeries.id === "TODO")  // TODO: assert id is ULID
            assert(maybeSeries.description.get === "Series Route Spec Description1")
            assert(maybeSeries.title === "Series Route Spec")
          }
        }
        .unsafeRunSync()
    }

    "delete a series" in {
      val series = seriesService.findByPath(s3.path).unsafeRunSync().get

      // 204 (first time)
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/series/${series.id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === NoContent)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
      assert(seriesService.findByPath(s3.path).unsafeRunSync().isEmpty)

      // 404 (second time)
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/series/${series.id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail.startsWith("series not found: "))
            assert(maybeError.instance === s"/v1/series/${series.id.value}")
          }
        }
        .unsafeRunSync()
    }

    "return 404 DELETE endopoint" in {
      val id = SeriesId(generateUlid())
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/series/${id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail.startsWith("series not found: "))
            assert(maybeError.instance === s"/v1/series/${id.value}")
          }
        }
        .unsafeRunSync()
    }

    "reject DELETE endpoint caused by invalid token" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/v1/series/reject", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + "invalid token"))))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "return 404" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/series/not-exists"))
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail === "series not found: /not-exists")
            assert(maybeError.instance === "/v1/series/not-exists")
          }
        }
        .unsafeRunSync()
    }

    "return Method Not Allowed" in {
      client
        .run(
          Request(
            method = Method.PATCH,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/series${s1.path.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === MethodNotAllowed)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "return 400 BadRequest caused by empty name" in {
      val json =
        """
          |{
          |  "name": "",
          |  "path": "400-badrequest-name-path",
          |  "title": "example series title",
          |  "description": "example series description"
          |}
        """.stripMargin

      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/v1/series/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Bad Request")
            assert(maybeError.status === 400)
            assert(maybeError.detail === "name is required")
            assert(maybeError.instance === "/v1/series/")
          }
        }
        .unsafeRunSync()
    }

    "return 400 BadRequest caused by empty path" in {
      val json =
        """
          |{
          |  "name": "400-badrequest-path-name",
          |  "path": "",
          |  "title": "example series title",
          |  "description": "example series description"
          |}
        """.stripMargin

      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/v1/series/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Bad Request")
            assert(maybeError.status === 400)
            assert(maybeError.detail === "path is required")
            assert(maybeError.instance === "/v1/series/")
          }
        }
        .unsafeRunSync()
    }

    "return 400 BadRequest caused by empty title" in {
      val json =
        """
          |{
          |  "name": "example-series-name",
          |  "path": "example-series-path",
          |  "title": "",
          |  "description": "example series description"
          |}
        """.stripMargin

      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/v1/series/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Bad Request")
            assert(maybeError.status === 400)
            assert(maybeError.detail === "title is required")
            assert(maybeError.instance === "/v1/series/")
          }
        }
        .unsafeRunSync()
    }

    "reject caused by expired token" in {
      val json =
        """
          |{
          |  "path": "example-series-path-expired-token",
          |  "title": "example series title",
          |  "description": "example series description"
          |}
        """.stripMargin
      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/v1/series/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + expiredToken)), entity = entity))
        .use { response =>
          IO {

            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "reject POST endpoint caused by the authorization header is empty" in {
      client
        .run(Request(method = Method.POST, uri = uri"/v1/series/"))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "reject POST endpoint caused by invalid token" in {
      val entity = EntityEncoder[IO, String].toEntity("")
      client
        .run(
          Request(method = Method.POST, uri = uri"/v1/series/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + "invalid Token")), entity = entity)
        )
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

  }

}
