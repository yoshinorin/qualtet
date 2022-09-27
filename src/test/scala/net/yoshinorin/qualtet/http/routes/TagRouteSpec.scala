package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.yoshinorin.qualtet.auth.RequestToken
import net.yoshinorin.qualtet.domains.authors.{AuthorName, ResponseAuthor}
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.tags.{TagId, ResponseTag}
import net.yoshinorin.qualtet.fixture.Fixture._
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.TagRouteSpec
class TagRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val validAuthor: ResponseAuthor = authorService.findByName(author.name).unsafeRunSync().get
  val validToken: String = authService.generateToken(RequestToken(validAuthor.id, "pass")).unsafeRunSync().token
  val tagRoute: TagRoute = new TagRoute(authService, tagService, articleService)

  val requestContents: List[RequestContent] = {
    (0 until 5).toList
      .map(_.toString())
      .map(i =>
        RequestContent(
          contentType = "article",
          path = Path(s"/test/tagRoute-${i}"),
          title = s"this is a tagRoute title ${i}",
          rawContent = s"this is a tagRoute raw content ${i}",
          htmlContent = s"this is a tagRoute html content ${i}",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          tags = List(s"tagRoute-${i}"),
          externalResources = List()
        )
      )
  }

  // NOTE: create content and related data for test
  requestContents.foreach { rc => contentService.createContentFromRequest(AuthorName(author.name.value), rc).unsafeRunSync() }

  val t: Seq[ResponseTag] = tagService.getAll.unsafeRunSync().filter(t => t.name.value.contains("tagRoute-"))

  "TagRoute" should {
    "be return tags" in {
      val expectJson =
        s"""
          |{
          |  "id" : "${t(0).id.value}",
          |  "name" : "${t(0).name.value}"
          |},
          |{
          |  "id" : "${t(1).id.value}",
          |  "name" : "${t(1).name.value}"
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      Get("/tags/") ~> tagRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains(expectJson))
      }
    }

    "be return specific tag" in {
      Get(s"/tags/${t(0).name.value}") ~> tagRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        // TODO: assert json
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains("/test/tagRoute-0"))
      }

      Get(s"/tags/${t(1).name.value}") ~> tagRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        // TODO: assert json
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains("/test/tagRoute-1"))
      }
    }

    "be return specific tag contents with query params" in {
      Get(s"/tags/${t(0).name.value}/?page=1&limit=10") ~> tagRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        // TODO: assert json
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains("/test/tagRoute-0"))
      }
    }

    "be return 10 specific tag contents with query params" in {
      Get(s"/tags/${t(0).name.value}/?page=1&limit=50") ~> tagRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        // TODO: assert json
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains("/test/tagRoute-0"))
        // TODO: assert json count
      }
    }

    "be return 500" in {
      Get("/tags/not-exists") ~> tagRoute.route ~> check {
        assert(status === StatusCodes.NotFound)
      }
    }

    "be delete a tag" in {
      val tag = tagService.findByName(t(4).name).unsafeRunSync().get

      // 204 (first time)
      Delete(s"/tags/${tag.id.value}")
        .addCredentials(OAuth2BearerToken(validToken)) ~> tagRoute.route ~> check {
        assert(status === StatusCodes.NoContent)
      }
      assert(tagService.findByName(t(4).name).unsafeRunSync().isEmpty)

      // 404 (second time)
      Delete(s"/tags/${tag.id.value}")
        .addCredentials(OAuth2BearerToken(validToken)) ~> tagRoute.route ~> check {
        assert(status === StatusCodes.NotFound)
      }
    }

    "be return 404 DELETE endopoint" in {
      val id = TagId(generateUlid())
      Delete(s"/tags/${id.value}")
        .addCredentials(OAuth2BearerToken(validToken)) ~> tagRoute.route ~> check {
        assert(status === StatusCodes.NotFound)
      }
    }

    "be reject DELETE endpoint caused by invalid token" in {
      Delete("/tags/reject")
        .addCredentials(OAuth2BearerToken("invalid token")) ~> tagRoute.route ~> check {
        // TODO: fix status code
        assert(status === StatusCodes.InternalServerError)
      }
    }

  }

}
