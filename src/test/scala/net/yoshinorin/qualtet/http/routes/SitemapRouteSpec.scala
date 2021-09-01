package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import net.yoshinorin.qualtet.domains.models.sitemaps.{LastMod, Loc, Url}
import net.yoshinorin.qualtet.domains.services.SitemapService
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.http.routes.SitemapRouteSpec
class SitemapRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val mockSitemapService: SitemapService = Mockito.mock(classOf[SitemapService])
  val sitemapRoute: SitemapRoute = new SitemapRoute(mockSitemapService)

  when(mockSitemapService.get()).thenReturn(
    IO(
      Seq(
        Url(Loc("https://example.com/aaa/bbb"), LastMod("1620738897")),
        Url(Loc("https://example.com/ccc/ddd"), LastMod("1620938897"))
      )
    )
  )

  "SitemapRoute" should {
    "return json for sitemap.xml" in {
      val expectJson =
        """
          |[
          |  {
          |    "loc" : "https://example.com/aaa/bbb",
          |    "lastMod" : "2021-05-11"
          |  },
          |  {
          |    "loc" : "https://example.com/ccc/ddd",
          |    "lastMod" : "2021-05-13"
          |  }
          |]
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      Get("/sitemaps/") ~> sitemapRoute.route ~> check {
        assert(status == StatusCodes.OK)
        assert(contentType == ContentTypes.`application/json`)
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "") == expectJson)
      }
    }
  }

}
