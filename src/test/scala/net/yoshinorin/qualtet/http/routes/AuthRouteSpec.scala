package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.Modules.*
import net.yoshinorin.qualtet.fixture.Fixture.{author, router}
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.AuthRouteSpec
class AuthRouteSpec extends AnyWordSpec {

  val a: ResponseAuthor = authorService.findByName(author.name).unsafeRunSync().get
  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "AuthRoute" should {

    "be return JWT correctly" in {
      val json =
        s"""
          |{
          |  "authorId" : "${a.id.value}",
          |  "password" : "pass"
          |}
        """.stripMargin

      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/token/", entity = entity))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))
            // TODO: assert json
            assert(response.as[String].unsafeRunSync().contains("."))
          }
        }
        .unsafeRunSync()
    }

    "be reject with unauthorized (wrong JSON format)" in {
      val wrongJsonFormat =
        s"""
          |{
          |  "authorId" : "${a.id.value}"
          |  "password" : "valid-password"
          |}
        """.stripMargin

      val entity = EntityEncoder[IO, String].toEntity(wrongJsonFormat)
      client
        .run(Request(method = Method.POST, uri = uri"/token/", entity = entity))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
          }
        }
        .unsafeRunSync()
    }

    "be reject with unauthorized (can not decode request JSON without password key)" in {
      val wrongJson =
        s"""
          |{
          |  "authorId" : "${a.id.value}"
          |}
        """.stripMargin

      val entity = EntityEncoder[IO, String].toEntity(wrongJson)
      client
        .run(Request(method = Method.POST, uri = uri"/token/", entity = entity))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
          }
        }
        .unsafeRunSync()
    }

    "be reject with unauthorized (can not decode request JSON without authorId key)" in {
      val wrongJson =
        """
          |{
          |  "password" : "valid-password"
          |}
        """.stripMargin

      val entity = EntityEncoder[IO, String].toEntity(wrongJson)
      client
        .run(Request(method = Method.POST, uri = uri"/token/", entity = entity))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
          }
        }
        .unsafeRunSync()
    }

    "be reject with wrong-password" in {
      val json =
        s"""
           |{
           |  "authorId" : "${a.id.value}",
           |  "password" : "wrong-pass"
           |}
        """.stripMargin

      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/token/", entity = entity))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
          }
        }
        .unsafeRunSync()
    }

    "be return if user not exists" in {
      val json =
        s"""
           |{
           |  "authorId" : "not-exists-user",
           |  "password" : "pass"
           |}
        """.stripMargin

      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/token/", entity = entity))
        .use { response =>
          IO {
            assert(response.status === NotFound)
            // TODO: avoid to return user not found message
            assert(response.as[String].unsafeRunSync().replaceAll("\n", "").replaceAll(" ", "").contains("not-exists-userisnotfound."))
          }
        }
        .unsafeRunSync()
    }

    "be return Method Not Allowed" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/token"))
        .use { response =>
          IO {
            assert(response.status === MethodNotAllowed)
          }
        }
        .unsafeRunSync()
    }
  }

}
