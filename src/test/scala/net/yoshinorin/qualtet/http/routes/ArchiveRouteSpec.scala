package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import net.yoshinorin.qualtet.domains.archives.{ArchiveService, ResponseArchive}
import net.yoshinorin.qualtet.domains.contents.Path
import net.yoshinorin.qualtet.fixture.Fixture
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.ArchiveRouteSpec
class ArchiveRouteSpec extends AnyWordSpec {

  val mockArchiveService: ArchiveService = Mockito.mock(classOf[ArchiveService])
  val archiveRoute: ArchiveRoute = new ArchiveRoute(mockArchiveService)

  val request: Request[IO] = Request(method = Method.GET, uri = uri"/archives")
  val client: Client[IO] = Client.fromHttpApp(Fixture.httpApp)

  when(mockArchiveService.get).thenReturn(
    IO(
      Seq(
        ResponseArchive(
          path = Path("/test/path1"),
          title = "title1",
          publishedAt = 1567814290
        ),
        ResponseArchive(
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
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      client.run(request).use { response =>
        IO {
          assert(response.status === Ok)
          assert(response.contentType.get === `Content-Type`(MediaType.application.json))
          assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "") === expectJson)
        }
      }.unsafeRunSync()
    }
  }
}
