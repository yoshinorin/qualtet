package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.yoshinorin.qualtet.domains.authors.{AuthorName, ResponseAuthor}
import net.yoshinorin.qualtet.fixture.Fixture.{author, author2, authorService}
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.AuthorRouteSpec
class AuthorRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val authorRoute: AuthorRoute = new AuthorRoute(authorService)

  val a: ResponseAuthor = authorService.findByName(AuthorName(author.name.value)).unsafeRunSync().get
  val a2: ResponseAuthor = authorService.findByName(AuthorName(author2.name.value)).unsafeRunSync().get

  "AuthorRoute" should {
    "be return two authors" in {
      val expectJson =
        s"""
          |{
          |  "id" : "${a.id.value}",
          |  "name" : "${a.name.value}",
          |  "displayName": "${a.displayName.value}",
          |  "createdAt": ${a.createdAt}
          |},
          |{
          |  "id" : "${a2.id.value}",
          |  "name" : "${a2.name.value}",
          |  "displayName": "${a2.displayName.value}",
          |  "createdAt": ${a2.createdAt}
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      Get("/authors/") ~> authorRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains(expectJson))
      }
    }

    "be return specific author" in {
      val expectJson =
        s"""
          |{
          |  "id": "${a.id.value}",
          |  "name": "${a.name.value}",
          |  "displayName": "${a.displayName.value}",
          |  "createdAt": ${a.createdAt}
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      Get("/authors/jhondue") ~> authorRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains(expectJson))
      }
    }

    "be return 404" in {
      Get("/authors/jhondue-not-exists") ~> authorRoute.route ~> check {
        assert(status === StatusCodes.NotFound)
        assert(contentType === ContentTypes.`application/json`)
      }
    }

  }

}
