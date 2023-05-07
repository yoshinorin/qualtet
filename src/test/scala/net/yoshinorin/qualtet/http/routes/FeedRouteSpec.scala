package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.fixture.Fixture.{author, router}
import net.yoshinorin.qualtet.Modules._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.FeedRouteSpec
class FeedRouteSpec extends AnyWordSpec with BeforeAndAfterAll {

  val requestContents: List[RequestContent] = {
    (0 until 2).toList
      .map(_.toString())
      .map(i =>
        RequestContent(
          contentType = "article",
          path = Path(s"/feeds/feedsRoute-${i}"),
          title = s"this is a feedsRoute title ${i}",
          rawContent = s"this is a feedsRoute raw content ${i}",
          htmlContent = s"this is a feedsRoute html content ${i}",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          tags = List(s"feedsRoute${i}"),
          externalResources = List()
        )
      )
  }

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    requestContents.foreach { rc => contentService.createContentFromRequest(AuthorName(author.name.value), rc).unsafeRunSync() }
  }

  val client: Client[IO] = Client.fromHttpApp(router.routes)

  "FeedRoute" should {
    "be return feeds" in {
      client
        .run(Request(method = Method.GET, uri = uri"/feeds/index"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            // TODO: assert response contents count
          }
        }
        .unsafeRunSync()
    }

    "be return Method Not Allowed" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/feeds/index"))
        .use { response =>
          IO {
            assert(response.status === MethodNotAllowed)
          }
        }
        .unsafeRunSync()
    }
  }

}
