package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import net.yoshinorin.qualtet.domains.models.authors.{AuthorDisplayName, AuthorId, AuthorName, ResponseAuthor}
import net.yoshinorin.qualtet.domains.services.AuthorService
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.http.routes.AuthorRouteSpec
class AuthorRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val mockAuthorService: AuthorService = Mockito.mock(classOf[AuthorService])
  val authorRoute: AuthorRoute = new AuthorRoute(mockAuthorService)

  when(mockAuthorService.getAll).thenReturn(
    IO(
      Seq(
        ResponseAuthor(
          id = AuthorId("01FEBB8AZ5T42M2H68XJ8C754A"),
          name = AuthorName("jhondue"),
          displayName = AuthorDisplayName("JD"),
          createdAt = 1567814290
        ),
        ResponseAuthor(
          id = AuthorId("01FEBB8AZ5T42M2H68XJ8C754B"),
          name = AuthorName("JhonDue2"),
          displayName = AuthorDisplayName("JD2"),
          createdAt = 1567814291
        )
      )
    )
  )

  when(mockAuthorService.findByName(AuthorName("jhondue"))).thenReturn(
    IO(
      Option(
        ResponseAuthor(
          id = AuthorId("01FEBB8AZ5T42M2H68XJ8C754A"),
          name = AuthorName("jhondue"),
          displayName = AuthorDisplayName("JD"),
          createdAt = 1567814290
        )
      )
    )
  )

  when(mockAuthorService.findByName(AuthorName("jhondue2"))).thenReturn(
    IO(None)
  )

  "AuthorRoute" should {
    "return all authors" in {
      val expectJson =
        """
          |[
          |  {
          |    "id" : "01FEBB8AZ5T42M2H68XJ8C754A",
          |    "name" : "jhondue",
          |    "displayName": "JD",
          |    "createdAt" : 1567814290
          |  },
          |  {
          |    "id" : "01FEBB8AZ5T42M2H68XJ8C754B",
          |    "name" : "jhondue2",
          |    "displayName": "JD2",
          |    "createdAt" : 1567814291
          |  }
          |]
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      Get("/authors/") ~> authorRoute.route ~> check {
        assert(status == StatusCodes.OK)
        assert(contentType == ContentTypes.`application/json`)
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "") == expectJson)
      }
    }

    "return specific author" in {
      val expectJson =
        """
          |{
          |  "id" : "01FEBB8AZ5T42M2H68XJ8C754A",
          |  "name" : "jhondue",
          |  "displayName": "JD",
          |  "createdAt" : 1567814290
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      Get("/authors/jhondue") ~> authorRoute.route ~> check {
        assert(status == StatusCodes.OK)
        assert(contentType == ContentTypes.`application/json`)
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "") == expectJson)
      }
    }

    "return 404" in {
      Get("/authors/jhondue2") ~> authorRoute.route ~> check {
        assert(status == StatusCodes.NotFound)
        assert(contentType == ContentTypes.`application/json`)
      }
    }

  }

}
