package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.Modules._
// import net.yoshinorin.qualtet.fixture.Fixture.{articleRoute, author}
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.ArticleRouteSpec
/*
class ArticleRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val requestContents: List[RequestContent] = {
    (0 until 20).toList
      .map(_.toString())
      .map(i =>
        RequestContent(
          contentType = "article",
          path = Path(s"/articles/route/article-${i}"),
          title = s"this is a articleRoute title ${i}",
          rawContent = s"this is a articleRoute raw content ${i}",
          htmlContent = s"this is a articleRoute html content ${i}",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          tags = List(s"articleRoute-${i}"),
          externalResources = List()
        )
      )
  }

  // NOTE: create content and related data for test
  requestContents.foreach { rc => contentService.createContentFromRequest(AuthorName(author.name.value), rc).unsafeRunSync() }

  "ArticleRoute" should {
    "be return articles with default query params" in {
      Get("/articles/") ~> articleRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        // TODO: assert json
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains("count"))
      }
    }

    "be return articles with query params" in {
      Get("/articles/?page=1&limit=5") ~> articleRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        // TODO: assert json
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains("count"))
        // TODO: assert json count is 5
      }
    }

    "be return 10 articles with query params" in {
      Get("/articles/?page=1&limit=50") ~> articleRoute.route ~> check {
        assert(status === StatusCodes.OK)
        assert(contentType === ContentTypes.`application/json`)
        // TODO: assert json
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "").contains("count"))
        // TODO: assert json count is 10
      }
    }

    "not be return articles with query params" in {
      Get("/articles/?page=99999&limit=10") ~> articleRoute.route ~> check {
        assert(status === StatusCodes.NotFound)
      }
    }

  }

}
*/
