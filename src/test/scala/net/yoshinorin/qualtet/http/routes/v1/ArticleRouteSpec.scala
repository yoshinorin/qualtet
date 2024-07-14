package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import net.yoshinorin.qualtet.domains.articles.ResponseArticleWithCount
import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.http.ResponseProblemDetails
import net.yoshinorin.qualtet.Modules.*
import net.yoshinorin.qualtet.fixture.Fixture.{author, router, unsafeDecode}
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.v1.ArticleRouteSpec
class ArticleRouteV1Spec extends AnyWordSpec {

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

  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "ArticleRoute" should {
    "return articles with default query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/articles"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))

            val maybeArticles = unsafeDecode[ResponseArticleWithCount](response)
            assert(maybeArticles.count >= 20) // FIXME: get number of all articles and assert it.
            assert(maybeArticles.articles.size === 10)
          }
        }
        .unsafeRunSync()
    }

    "return articles with query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/articles/?page=1&limit=5"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))

            val maybeArticles = unsafeDecode[ResponseArticleWithCount](response)
            assert(maybeArticles.count >= 20) // FIXME: get number of all articles and assert it.
            assert(maybeArticles.articles.size === 5)
          }
        }
        .unsafeRunSync()
    }

    "return 10 articles with query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/articles?page=1&limit=50"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))

            val maybeArticles = unsafeDecode[ResponseArticleWithCount](response)
            assert(maybeArticles.count >= 20) // FIXME: get number of all articles and assert it.
            assert(maybeArticles.articles.size === 10)
          }
        }
        .unsafeRunSync()
    }

    "not be return articles with query params" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/articles?page=9999&limit=10"))
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail === "articles not found")
            assert(maybeError.instance === "/v1/articles?page=9999&limit=10")
          }
        }
        .unsafeRunSync()
    }

    "return Method Not Allowed" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/v1/articles"))
        .use { response =>
          IO {
            assert(response.status === MethodNotAllowed)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

  }

}
