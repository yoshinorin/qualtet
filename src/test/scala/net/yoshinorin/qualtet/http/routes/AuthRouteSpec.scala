package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import net.yoshinorin.qualtet.auth.{AuthService, RequestToken, ResponseToken}
import net.yoshinorin.qualtet.domains.models.authors.AuthorId
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.http.routes.AuthRouteSpec
class AuthRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val mockAuthService: AuthService = Mockito.mock(classOf[AuthService])
  val authRoute: AuthRoute = new AuthRoute(mockAuthService)

  "ApiStatusRoute" should {

    "return JWT correctly" in {
      when(
        mockAuthService.generateToken(RequestToken(AuthorId("01FEBB8AZ5T42M2H68XJ8C754A"), "valid-password"))
      ).thenReturn(
        IO(
          ResponseToken(
            "valid.token"
          )
        )
      )

      val json =
        """
          |{
          |  "authorId" : "01FEBB8AZ5T42M2H68XJ8C754A",
          |  "password" : "valid-password"
          |}
        """.stripMargin

      Post("/token/")
        .withEntity(ContentTypes.`application/json`, json) ~> authRoute.route ~> check {
        assert(status == StatusCodes.Created)
        assert(contentType == ContentTypes.`application/json`)
        assert(responseAs[String].contains("valid.token"))
      }
    }

    "reject with bad request (wrong JSON format)" in {
      val wrongJsonFormat =
        """
          |{
          |  "authorId" : "01FEBB8AZ5T42M2H68XJ8C754A"
          |  "password" : "valid-password"
          |}
        """.stripMargin

      Post("/token/")
        .withEntity(ContentTypes.`application/json`, wrongJsonFormat) ~> authRoute.route ~> check {
        assert(status == StatusCodes.BadRequest)
      }
    }

    "reject with bad request (can not decode request JSON without password key)" in {
      val wrongJson =
        """
          |{
          |  "authorId" : "01FEBB8AZ5T42M2H68XJ8C754A"
          |}
        """.stripMargin

      Post("/token/")
        .withEntity(ContentTypes.`application/json`, wrongJson) ~> authRoute.route ~> check {
        assert(status == StatusCodes.BadRequest)
      }
    }

    "reject with bad request (can not decode request JSON without authorId key)" in {
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
