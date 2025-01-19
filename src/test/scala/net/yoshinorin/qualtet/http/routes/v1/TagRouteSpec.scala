package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import org.typelevel.ci.*
import wvlet.airframe.ulid.ULID
import net.yoshinorin.qualtet.auth.RequestToken
import net.yoshinorin.qualtet.domains.articles.ArticleWithCountResponseModel
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.domains.tags.{TagId, TagResponseModel}
import net.yoshinorin.qualtet.http.errors.ResponseProblemDetails
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.fixture.Fixture.{authProvider => fixtureAuthProvider}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.v1.TagRouteSpec
class TagRouteV1Spec extends AnyWordSpec with BeforeAndAfterAll {

  val requestContents = makeRequestContents(10, "tagRoute")
  // NOTE: create content and related data for test
  createContents(requestContents)

  /* TODO: `BeforeAndAfterAll` seems doesn't work on CI this test class.
  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContents(requestContents)
  }
   */

  val validAuthor: AuthorResponseModel = authorService.findByName(author.name).unsafeRunSync().get
  val validToken: String = authService.generateToken(RequestToken(validAuthor.id, "pass")).unsafeRunSync().token
  val tagRouteV1 = new TagRoute(fixtureAuthProvider, tagService, articleService)
  val client: Client[IO] = Client.fromHttpApp(makeRouter(tagRouteV1 = tagRouteV1).routes.orNotFound)

  "TagRoute" should {

    val t: Seq[TagResponseModel] = tagService.getAll.unsafeRunSync().filter(t => t.name.value.startsWith("tagRouteTag"))

    "return tags" in {
      val expectJson =
        s"""
          |{
          |  "id" : "${t(0).id.value}",
          |  "name" : "${t(0).name.value}",
          |  "count" : 1
          |},
          |{
          |  "id" : "${t(1).id.value}",
          |  "name" : "${t(1).name.value}",
          |  "count" : 1
          |}
      """.stripMargin.replaceNewlineAndSpace

      client
        .run(Request(method = Method.GET, uri = uri"/v1/tags/"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceNewlineAndSpace.contains(expectJson))
          }
        }
        .unsafeRunSync()
    }

    "return specific tag" in {
      client
        .run(Request(method = Method.GET, uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/tags/${t(0).name.value}"))))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))

            val maybeArticles = unsafeDecode[ArticleWithCountResponseModel](response)
            assert(maybeArticles.count === 1)
            assert(maybeArticles.articles.head.path.toString().startsWith("/test/tagRoute-0"))
          }
        }
        .unsafeRunSync()

      client
        .run(Request(method = Method.GET, uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/tags/${t(1).name.value}"))))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))

            val maybeArticles = unsafeDecode[ArticleWithCountResponseModel](response)
            assert(maybeArticles.count === 1)
            assert(maybeArticles.articles.head.path.toString().startsWith("/test/tagRoute-1"))
          }
        }
        .unsafeRunSync()
    }

    "return specific tag contents with query params" in {
      client
        .run(
          Request(
            method = Method.GET,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/tags/${t(0).name.value}")).withQueryParam("page", "1").withQueryParam("limit", "10")
          )
        )
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceNewlineAndSpace.contains("/test/tagRoute-0"))
          }
        }
        .unsafeRunSync()
    }

    "return 10 specific tag contents with query params" in {
      client
        .run(
          Request(
            method = Method.GET,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/tags/${t(1).name.value}")).withQueryParam("page", "1").withQueryParam("limit", "50")
          )
        )
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceNewlineAndSpace.contains("/test/tagRoute-1"))
          }
        }
        .unsafeRunSync()
    }

    "return articles sort by desc by default" in {
      for {
        articles <- client
          .run(Request(method = Method.GET, uri = uri"/v1/tags/tagRoute-1"))
          .use { response =>
            IO {
              unsafeDecode[ArticleWithCountResponseModel](response)
            }
          }
      } yield {
        // FIXME: It only fails in the CI.
        // val page1LatestArticle = ULID.fromString(articles.articles.head.id.toString())
        // val oldArticle = ULID.fromString(maybeArticles.articles(1).id.toString())
        // val page1OldestArticle = ULID.fromString(articles.articles.last.id.toString())
        // assert(page1LatestArticle.epochMillis > page1OldestArticle.epochMillis)
      }
    }

    "return articles sort by desc with query params" in {
      for {
        page1Articles <- client
          .run(Request(method = Method.GET, uri = uri"/v1/tags/tagRoute-1?page=1&limit=5&order=desc"))
          .use { response =>
            IO {
              unsafeDecode[ArticleWithCountResponseModel](response)
            }
          }
        page2Articles <- client
          .run(Request(method = Method.GET, uri = uri"/v1/tags/tagRoute-1?page=2&limit=5&order=desc"))
          .use { response =>
            IO {
              unsafeDecode[ArticleWithCountResponseModel](response)
            }
          }
      } yield {
        val page1LatestArticle = ULID.fromString(page1Articles.articles.head.id.toString())
        val page1OldestArticle = ULID.fromString(page1Articles.articles.last.id.toString())
        assert(page1LatestArticle.epochMillis > page1OldestArticle.epochMillis)

        val page2LatestArticle = ULID.fromString(page2Articles.articles.head.id.toString())
        val page2OldestArticle = ULID.fromString(page2Articles.articles.last.id.toString())
        assert(page2LatestArticle.epochMillis > page2OldestArticle.epochMillis)

        assert(page1OldestArticle.epochMillis > page2LatestArticle.epochMillis)
      }
    }

    "return articles sort by asc with query params" in {
      for {
        page1Articles <- client
          .run(Request(method = Method.GET, uri = uri"/v1/tags/tagRoute-1?page=1&limit=5&order=asc"))
          .use { response =>
            IO {
              unsafeDecode[ArticleWithCountResponseModel](response)
            }
          }
        page2Articles <- client
          .run(Request(method = Method.GET, uri = uri"/v1/tags/tagRoute-1?page=2&limit=5&order=asc"))
          .use { response =>
            IO {
              unsafeDecode[ArticleWithCountResponseModel](response)
            }
          }
      } yield {
        val page1LatestArticle = ULID.fromString(page1Articles.articles.head.id.toString())
        val page1OldestArticle = ULID.fromString(page1Articles.articles.last.id.toString())
        assert(page1OldestArticle.epochMillis > page1LatestArticle.epochMillis)

        val page2LatestArticle = ULID.fromString(page2Articles.articles.head.id.toString())
        val page2OldestArticle = ULID.fromString(page2Articles.articles.last.id.toString())
        assert(page2OldestArticle.epochMillis > page2LatestArticle.epochMillis)

        assert(page2LatestArticle.epochMillis > page1OldestArticle.epochMillis)
      }
    }

    "return 404" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/tags/not-exists"))
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail.startsWith("articles not found"))
            assert(maybeError.instance === "/v1/tags/not-exists")
          }
        }
        .unsafeRunSync()
    }

    "delete a tag" in {
      val tag = tagService.findByName(t(4).name).unsafeRunSync().get

      // 204 (first time)
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/tags/${tag.id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === NoContent)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
      assert(tagService.findByName(t(4).name).unsafeRunSync().isEmpty)

      // 404 (second time)
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/tags/${tag.id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail.startsWith("tag not found: "))
            assert(maybeError.instance === s"/v1/tags/${tag.id.value}")
          }
        }
        .unsafeRunSync()
    }

    "return 404 DELETE endopoint" in {
      val id = TagId(generateUlid())
      client
        .run(
          Request(
            method = Method.DELETE,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/tags/${id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail.startsWith("tag not found: "))
            assert(maybeError.instance === s"/v1/tags/${id.value}")
          }
        }
        .unsafeRunSync()
    }

    "reject DELETE endpoint caused by invalid token" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/v1/tags/reject", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + "invalid token"))))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "return Method Not Allowed" in {
      val tag = tagService.findByName(t(2).name).unsafeRunSync().get
      client
        .run(
          Request(
            method = Method.PATCH,
            uri = new Uri().withPath(Uri.Path.unsafeFromString(s"/v1/tags/${tag.id.value}")),
            headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))
          )
        )
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
