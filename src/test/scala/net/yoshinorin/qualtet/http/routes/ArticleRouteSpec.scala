package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.Modules.*
import net.yoshinorin.qualtet.fixture.Fixture
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.ArticleRouteSpec
class ArticleRouteSpec extends AnyWordSpec {

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
  requestContents.foreach { rc => contentService.createContentFromRequest(AuthorName(Fixture.author.name.value), rc).unsafeRunSync() }

  val client: Client[IO] = Client.fromHttpApp(Fixture.router.routes)

  "ArticleRoute" should {
    "be return articles with default query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/articles"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            // TODO: assert json
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("count"))
          }
        }
        .unsafeRunSync()
    }

    "be return articles with query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/articles/?page=1&limit=5"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            // TODO: assert json
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("count"))
          }
        }
        .unsafeRunSync()
    }

    "be return 10 articles with query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/articles?page=1&limit=50"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            // TODO: assert json & it's count
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("count"))
          }
        }
        .unsafeRunSync()
    }

    "not be return articles with query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/articles?page=9999&limit=10"))
        .use { response =>
          IO {
            assert(response.status === NotFound)
          }
        }
        .unsafeRunSync()
    }

    "be return Method Not Allowed" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/articles"))
        .use { response =>
          IO {
            assert(response.status === MethodNotAllowed)
          }
        }
        .unsafeRunSync()
    }

  }

}
