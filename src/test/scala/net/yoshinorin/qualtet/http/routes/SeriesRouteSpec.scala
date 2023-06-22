package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import org.typelevel.ci._
import net.yoshinorin.qualtet.auth.RequestToken
import net.yoshinorin.qualtet.domains.authors.{AuthorName, ResponseAuthor}
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.series.{Series, SeriesName, RequestSeries}
import net.yoshinorin.qualtet.fixture.Fixture._
import net.yoshinorin.qualtet.Modules._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.SeriesRouteSpec
class SeriesRouteSpec extends AnyWordSpec with BeforeAndAfterAll {

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

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createSeries(requestSeries)
    createContents(requestContents)
  }

  val validAuthor: ResponseAuthor = authorService.findByName(author.name).unsafeRunSync().get
  val validToken: String = authService.generateToken(RequestToken(validAuthor.id, "pass")).unsafeRunSync().token
  val seriesRoute = new SeriesRoute(seriesService)
  val client: Client[IO] = Client.fromHttpApp(makeRouter(seriesRoute = seriesRoute).routes)

  "SeriesRoute" should {

    val s1: Series = seriesService.findByName(requestSeries.head.name).unsafeRunSync().get
    val s2: Series = seriesService.findByName(requestSeries(1).name).unsafeRunSync().get

    "be create a series" in {
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
        .run(Request(method = Method.POST, uri = uri"/series/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === Created)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            // TODO: assert response
          }
        }
        .unsafeRunSync()
    }

    "be return series" in {
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
        .run(Request(method = Method.GET, uri = uri"/series/"))
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

    "be return specific series" in {
      client
        .run(Request(method = Method.GET, uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/series/${s1.name.value}"))))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("seriesroute-series"))
          }
        }
        .unsafeRunSync()
    }

    "be return 404" in {
      client
        .run(Request(method = Method.GET, uri = uri"/series/not-exists"))
        .use { response =>
          IO {
            assert(response.status === NotFound)
          }
        }
        .unsafeRunSync()
    }

    "be return Method Not Allowed" in {
      client
        .run(
          Request(
            method = Method.PATCH,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/series/${s1.name.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === MethodNotAllowed)
          }
        }
        .unsafeRunSync()
    }

    "be return 400 BadRequest caused by empty name" in {
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
        .run(Request(method = Method.POST, uri = uri"/series/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
          }
        }
        .unsafeRunSync()
    }

    "be return 400 BadRequest caused by empty title" in {
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
        .run(Request(method = Method.POST, uri = uri"/series/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken)), entity = entity))
        .use { response =>
          IO {
            assert(response.status === BadRequest)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
          }
        }
        .unsafeRunSync()
    }

    "be reject caused by expired token" ignore {
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
        .run(Request(method = Method.POST, uri = uri"/series/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + expiredToken)), entity = entity))
        .use { response =>
          IO {
            // TODO: assert response
            // assert(response.status === Unauthorized)
          }
        }
      // .unsafeRunSync()
    }

    "be reject POST endpoint caused by the authorization header is empty" ignore {
      client
        .run(Request(method = Method.POST, uri = uri"/series/"))
        .use { response =>
          IO {
            // TODO: assert(response.status === Unauthorized)
          }
        }
      // TODO: .unsafeRunSync()
    }

    "be reject POST endpoint caused by invalid token" ignore {
      val entity = EntityEncoder[IO, String].toEntity("")
      client
        .run(
          Request(method = Method.POST, uri = uri"/series/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + "invalid Token")), entity = entity)
        )
        .use { response =>
          IO {
            // TODO: assert(response.status === Unauthorized)
          }
        }
      // TODO: .unsafeRunSync()
    }

  }

}
