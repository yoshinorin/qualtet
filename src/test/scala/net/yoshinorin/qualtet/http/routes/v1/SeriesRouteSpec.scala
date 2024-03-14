package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import org.typelevel.ci.*
import net.yoshinorin.qualtet.auth.RequestToken
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.domains.series.{RequestSeries, Series, SeriesName}
import net.yoshinorin.qualtet.message.ProblemDetails
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.Modules.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.v1.SeriesRouteSpec
class SeriesRouteV1Spec extends AnyWordSpec with BeforeAndAfterAll {

  val requestSeries: List[RequestSeries] = List(
    RequestSeries(
      title = "Series Route Spec",
      name = SeriesName("seriesroute-series"),
      description = Some("Series Route Spec Description1")
    ),
    RequestSeries(
      title = "Series Route Spec2",
      name = SeriesName("seriesroute-series2"),
      description = Some("Series Route Spec Description2")
    )
  )

  val requestContents = makeRequestContents(5, "SeriesRoute", Some(requestSeries.head.name))
  createSeries(requestSeries)
  createContents(requestContents)

  /* TODO: `BeforeAndAfterAll` seems doesn't work on CI this test class.
  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createSeries(requestSeries)
    createContents(requestContents)
  }
   */

  val validAuthor: ResponseAuthor = authorService.findByName(author.name).unsafeRunSync().get
  val validToken: String = authService.generateToken(RequestToken(validAuthor.id, "pass")).unsafeRunSync().token
  val seriesRouteV1 = new SeriesRoute(authProvider, seriesService)
  val client: Client[IO] = Client.fromHttpApp(makeRouter(seriesRouteV1 = seriesRouteV1).routes.orNotFound)

  "SeriesRoute" should {

    val s1: Series = seriesService.findByName(requestSeries.head.name).unsafeRunSync().get
    val s2: Series = seriesService.findByName(requestSeries(1).name).unsafeRunSync().get

    "create a series" in {
      val json =
        """
          |{
          |  "name": "example series",
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
          |  "title" : "${s1.title}",
          |  "description" : "${s1.description.get}"
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      val expectPartialJson2 =
        s"""
          |{
          |  "id" : "${s2.id.value}",
          |  "name" : "${s2.name.value}",
          |  "title" : "${s2.title}",
          |  "description" : "${s2.description.get}"
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      client
        .run(Request(method = Method.GET, uri = uri"/v1/series/"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains(expectPartialJson))
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains(expectPartialJson2))
          }
        }
        .unsafeRunSync()
    }

    "return specific series" in {
      client
        .run(Request(method = Method.GET, uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/series/${s1.name.value}"))))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("seriesroute-series"))

            val maybeSeries = unsafeDecode[Series](response)
            assert(maybeSeries.name === "seriesroute-series")
            // assert(maybeSeries.id === "TODO")  // TODO: assert id is ULID
            assert(maybeSeries.description.get === "Series Route Spec Description1")
            assert(maybeSeries.title === "Series Route Spec")
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

            val maybeError = unsafeDecode[ProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail === "series not found: not-exists")
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
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/series/${s1.name.value}")),
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

            val maybeError = unsafeDecode[ProblemDetails](response)
            assert(maybeError.title === "Bad Request")
            assert(maybeError.status === 400)
            assert(maybeError.detail === "name is required")
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

            val maybeError = unsafeDecode[ProblemDetails](response)
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
          |  "name": "example series",
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
