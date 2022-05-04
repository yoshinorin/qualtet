package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.yoshinorin.qualtet.fixture.Fixture.contentTypeRoute
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.http.routes.ContentTypeRouteSpec
class ContentTypeRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  "ContentTypeRoute" should {

    "be return content-types" in {
      Get("/content-types/") ~> contentTypeRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        // TODO: assert response contents count
      }
    }

    "be return content-type:articles" in {
      Get("/content-types/article") ~> contentTypeRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains("article"))
      }
    }

    "be return content-type:page" in {
      Get("/content-types/page") ~> contentTypeRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains("page"))
      }
    }

    "be return content-type:not-exists" in {
      Get("/content-types/not-exists") ~> contentTypeRoute.route ~> check {
        assert(status === StatusCodes.NotFound)
      }
    }
  }

}
