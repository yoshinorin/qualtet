package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.yoshinorin.qualtet.domains.models.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.models.robots.Attributes
import net.yoshinorin.qualtet.fixture.Fixture.feedRoute
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.http.routes.FeedRouteSpec
class FeedRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val requestContents: List[RequestContent] = {
    (0 until 2).toList.map(i =>
      RequestContent(
        contentType = "article",
        path = Path(s"/feeds/feedsRoute-${i}"),
        title = s"this is a feedsRoute title ${i}",
        rawContent = s"this is a feedsRoute raw content ${i}",
        htmlContent = Option(s"this is a feedsRoute html content ${i}"),
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = Option(List(s"feedsRoute${i}")),
        externalResources = Option(List())
      )
    )
  }

  "FeedRoute" should {
    "be return feeds" in {
      Get("/feeds/index") ~> feedRoute.route ~> check {
        assert(status == StatusCodes.OK)
        assert(contentType == ContentTypes.`application/json`)
        // TODO: assert response contents count
      }
    }
  }

}
