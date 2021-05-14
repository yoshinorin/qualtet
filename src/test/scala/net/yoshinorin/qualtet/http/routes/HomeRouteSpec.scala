package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.http.routes.HomeRouteSpec
class HomeRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val homeRoute = new HomeRoute()

  "HomeRoute" should {

    "hello Qualtet!!" in {
      Get("/") ~> homeRoute.route ~> check {
        assert(status == StatusCodes.OK)
        assert(contentType == ContentTypes.`text/plain(UTF-8)`)
        assert(responseAs[String].contains("Hello Qualtet!!"))
      }
    }
  }

}
