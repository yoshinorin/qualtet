package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import net.yoshinorin.qualtet.domains.authors.{AuthorName, ResponseAuthor}
import net.yoshinorin.qualtet.fixture.Fixture.{author, author2, router}
import net.yoshinorin.qualtet.Modules.*
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.AuthorRouteSpec
class AuthorRouteSpec extends AnyWordSpec {

  val authorRoute = new AuthorRoute(authorService)

  val a: ResponseAuthor = authorService.findByName(AuthorName(author.name.value)).unsafeRunSync().get
  val a2: ResponseAuthor = authorService.findByName(AuthorName(author2.name.value)).unsafeRunSync().get

  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

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

      client
        .run(Request(method = Method.GET, uri = uri"/authors/"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            // TODO: assert json & it's count
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains(expectJson))
          }
        }
        .unsafeRunSync()
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

      client
        .run(Request(method = Method.GET, uri = uri"/authors/jhondue"))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            // TODO: assert json & it's count
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains(expectJson))
          }
        }
        .unsafeRunSync()
    }

    "be return 404" in {
      client
        .run(Request(method = Method.GET, uri = uri"/authors/jhondue-not-exists"))
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("NotFound"))
          }
        }
        .unsafeRunSync()
    }

    "be return Method Not Allowed" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/authors"))
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
