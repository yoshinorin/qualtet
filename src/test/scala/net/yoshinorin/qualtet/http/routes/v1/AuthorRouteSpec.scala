package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.domains.authors.{AuthorName, AuthorResponseModel}
import net.yoshinorin.qualtet.http.errors.ResponseProblemDetails
import net.yoshinorin.qualtet.fixture.Fixture.{author, author2, authorService, router, unsafeDecode}
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.v1.AuthorRouteSpec
class AuthorRouteV1Spec extends AnyWordSpec {

  val authorRoute = new AuthorRoute(authorService)

  val a: AuthorResponseModel = authorService.findByName(AuthorName(author.name.value)).unsafeRunSync().get
  val a2: AuthorResponseModel = authorService.findByName(AuthorName(author2.name.value)).unsafeRunSync().get

  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "AuthorRoute" should {
    "return two authors" in {
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
      """.stripMargin.replaceNewlineAndSpace

      client
        .run(Request(method = Method.GET, uri = uri"/v1/authors/"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceNewlineAndSpace.contains(expectJson))
          }
        }
        .unsafeRunSync()
    }

    "return specific author" in {
      val expectJson =
        s"""
          |{
          |  "id": "${a.id.value}",
          |  "name": "${a.name.value}",
          |  "displayName": "${a.displayName.value}",
          |  "createdAt": ${a.createdAt}
          |}
      """.stripMargin.replaceNewlineAndSpace

      client
        .run(Request(method = Method.GET, uri = uri"/v1/authors/jhondue"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceNewlineAndSpace.contains(expectJson))
          }
        }
        .unsafeRunSync()
    }

    "return 404" in {
      client
        .run(Request(method = Method.GET, uri = uri"/v1/authors/jhondue-not-exists"))
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))

            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail === "Not Found")
            assert(maybeError.instance === "/v1/authors/jhondue-not-exists")
          }
        }
        .unsafeRunSync()
    }

    "return Method Not Allowed" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/v1/authors"))
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
