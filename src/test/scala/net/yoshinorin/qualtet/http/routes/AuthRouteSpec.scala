package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.yoshinorin.qualtet.fixture.Fixture.{authRoute, author, authorService}
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.http.routes.AuthRouteSpec
class AuthRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val a = authorService.findByName(author.name).unsafeRunSync().get

  "ApiStatusRoute" should {

    "be return JWT correctly" in {
      val json =
        s"""
          |{
          |  "authorId" : "${a.id.value}",
          |  "password" : "pass"
          |}
        """.stripMargin

      Post("/token/")
        .withEntity(ContentTypes.`application/json`, json) ~> authRoute.route ~> check {
        assert(status == StatusCodes.Created)
        assert(contentType == ContentTypes.`application/json`)
        println(responseAs[String])
        assert(responseAs[String].contains("."))
      }
    }

    "be reject with bad request (wrong JSON format)" in {
      val wrongJsonFormat =
        s"""
          |{
          |  "authorId" : "${a.id.value}"
          |  "password" : "valid-password"
          |}
        """.stripMargin

      Post("/token/")
        .withEntity(ContentTypes.`application/json`, wrongJsonFormat) ~> authRoute.route ~> check {
        assert(status == StatusCodes.BadRequest)
      }
    }

    "be reject with bad request (can not decode request JSON without password key)" in {
      val wrongJson =
        s"""
          |{
          |  "authorId" : "${a.id.value}"
          |}
        """.stripMargin

      Post("/token/")
        .withEntity(ContentTypes.`application/json`, wrongJson) ~> authRoute.route ~> check {
        assert(status == StatusCodes.BadRequest)
      }
    }

    "be reject with bad request (can not decode request JSON without authorId key)" in {
      val wrongJson =
        """
          |{
          |  "password" : "valid-password"
          |}
        """.stripMargin

      Post("/token/")
        .withEntity(ContentTypes.`application/json`, wrongJson) ~> authRoute.route ~> check {
        assert(status == StatusCodes.BadRequest)
      }
    }

    // TODO: 404 and 401

  }

}
