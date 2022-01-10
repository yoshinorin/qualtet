package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import net.yoshinorin.qualtet.domains.models.authors.{AuthorDisplayName, AuthorId, AuthorName, ResponseAuthor}
import net.yoshinorin.qualtet.domains.services.AuthorService
import net.yoshinorin.qualtet.fixture.Fixture.{authorId, authorId2}
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
          id = authorId,
          name = AuthorName("jhondue"),
          displayName = AuthorDisplayName("JD"),
          createdAt = 1567814290
        ),
        ResponseAuthor(
          id = authorId2,
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
          id = authorId,
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
          |    "id" : "01febb8az5t42m2h68xj8c754a",
          |    "name" : "jhondue",
          |    "displayName": "JD",
          |    "createdAt" : 1567814290
          |  },
          |  {
          |    "id" : "01febb8az5t42m2h68xj8c754b",
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
          |  "id" : "01febb8az5t42m2h68xj8c754a",
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
