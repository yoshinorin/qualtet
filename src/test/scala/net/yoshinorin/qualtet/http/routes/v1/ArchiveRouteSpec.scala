package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import doobie.ConnectionIO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import net.yoshinorin.qualtet.domains.archives.{ArchiveResponseModel, ArchiveService}
import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.v1.ArchiveRouteSpec
class ArchiveRouteSpec extends AnyWordSpec {

  val mockArchiveService = Mockito.mock(classOf[ArchiveService[ConnectionIO]])
  val archiveRouteV1 = new ArchiveRoute(mockArchiveService)

  val router = makeRouter(archiveRouteV1 = archiveRouteV1)

  val request: Request[IO] = Request(method = Method.GET, uri = uri"/v1/archives")
  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  when(mockArchiveService.get).thenReturn(
    IO(
      Seq(
        ArchiveResponseModel(
          path = Path("/test/path1"),
          title = "title1",
          publishedAt = 1567814290
        ),
        ArchiveResponseModel(
          path = Path("/test/path2"),
          title = "title2",
          publishedAt = 1567814391
        )
      )
    )
  )

  "ArchiveRoute" should {
    "return all archives" in {
      val expectJson =
        """
          |[
          |  {
          |    "path" : "/test/path1",
          |    "title" : "title1",
          |    "publishedAt" : 1567814290
          |  },
          |  {
          |    "path" : "/test/path2",
          |    "title" : "title2",
          |    "publishedAt" : 1567814391
          |  }
          |]
      """.stripMargin.replaceNewlineAndSpace

      client
        .run(request)
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceNewlineAndSpace === expectJson)
          }
        }
        .unsafeRunSync()
    }
  }

  "return Method Not Allowed" in {
    client
      .run(Request(method = Method.DELETE, uri = uri"/v1/archives"))
      .use { response =>
        IO {
          assert(response.status === MethodNotAllowed)
          assert(response.contentType.isEmpty)
        }
      }
      .unsafeRunSync()
  }
}
