package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.fixture.Fixture.{author, contentService, sitemapRoute}
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.SitemapRouteSpec
class SitemapRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val requestContents: List[RequestContent] = {
    (0 until 2).toList.map(_.toString()).map(i =>
      RequestContent(
        contentType = "article",
        path = Path(s"/test/sitemapRoute-${i}"),
        title = s"this is a sitemapRoute title ${i}",
        rawContent = s"this is a sitemapRoute raw content ${i}",
        htmlContent = s"this is a sitemapRoute html content ${i}",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = Option(List(s"sitemapRoute-${i}")),
        externalResources = Option(List())
      )
    )
  }

  // NOTE: create content and related data for test
  requestContents.foreach { rc => contentService.createContentFromRequest(AuthorName(author.name.value), rc).unsafeRunSync() }

  "SitemapRoute" should {
    "be return json for sitemap.xml" in {
      Get("/sitemaps/") ~> sitemapRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        // TODO: assert JSON and count
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains("loc"))
      }
    }
  }

}
