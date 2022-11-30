package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import net.yoshinorin.qualtet.domains.archives.{ArchiveService, ResponseArchive}
import net.yoshinorin.qualtet.domains.contents.Path
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.http.routes.ArchiveRouteSpec
class ArchiveRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val mockArchiveService: ArchiveService = Mockito.mock(classOf[ArchiveService])
  val archiveRoute: ArchiveRoute = new ArchiveRoute(mockArchiveService)

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

      /*
      Get("/archives/") ~> archiveRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "") === expectJson)
      }
      */
    }

  }

}
