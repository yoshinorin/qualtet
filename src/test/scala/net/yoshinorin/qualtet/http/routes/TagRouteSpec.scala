package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.yoshinorin.qualtet.domains.models.authors.AuthorName
import net.yoshinorin.qualtet.domains.models.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.models.robots.Attributes
import net.yoshinorin.qualtet.domains.models.tags.ResponseTag
import net.yoshinorin.qualtet.fixture.Fixture.{articleService, author, contentService, tagService}
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.http.routes.TagRouteSpec
class TagRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val tagRoute: TagRoute = new TagRoute(tagService, articleService)

  val requestContents: List[RequestContent] = {
    (0 until 2).toList.map(i =>
      RequestContent(
        contentType = "article",
        path = Path(s"/test/tagRoute-${i}"),
        title = s"this is a tagRoute title ${i}",
        rawContent = s"this is a tagRoute raw content ${i}",
        htmlContent = Option(s"this is a tagRoute html content ${i}"),
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = Option(List(s"tagRoute-${i}")),
        externalResources = Option(List())
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
        assert(status == StatusCodes.OK)
        assert(contentType == ContentTypes.`application/json`)
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains(expectJson))
      }
    }

    "be return specific tag" in {
      Get(s"/tags/${t(0).name.value}") ~> tagRoute.route ~> check {
        assert(status == StatusCodes.OK)
        assert(contentType == ContentTypes.`application/json`)
        // TODO: assert json
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains("/test/tagRoute-0"))
      }

      Get(s"/tags/${t(1).name.value}") ~> tagRoute.route ~> check {
        assert(status == StatusCodes.OK)
        assert(contentType == ContentTypes.`application/json`)
        // TODO: assert json
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains("/test/tagRoute-1"))
      }
    }

    "be return specific tag contents with query params" in {
      Get(s"/tags/${t(0).name.value}/?page=1&limit=10") ~> tagRoute.route ~> check {
        assert(status == StatusCodes.OK)
        assert(contentType == ContentTypes.`application/json`)
        // TODO: assert json
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains("/test/tagRoute-0"))
      }
    }

    "be return 10 specific tag contents with query params" in {
      Get(s"/tags/${t(0).name.value}/?page=1&limit=50") ~> tagRoute.route ~> check {
        assert(status == StatusCodes.OK)
        assert(contentType == ContentTypes.`application/json`)
        // TODO: assert json
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains("/test/tagRoute-0"))
        // TODO: assert json count
      }
    }

    "be return 500" in {
      Get("/tags/not-exists") ~> tagRoute.route ~> check {
        assert(status == StatusCodes.NotFound)
      }
    }

  }

}
