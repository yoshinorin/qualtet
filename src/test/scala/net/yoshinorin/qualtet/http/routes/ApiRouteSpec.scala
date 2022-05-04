package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.http.routes.ApiRouteSpec
class ApiRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val apiStatusRoute = new ApiStatusRoute()

  "ApiStatusRoute" should {

    "return operational JSON" in {
      Get("/status/") ~> apiStatusRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "") === "{\"status\":\"operational\"}")
      }

    }
  }

}
