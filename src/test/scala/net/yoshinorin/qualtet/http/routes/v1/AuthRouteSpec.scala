package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel
import net.yoshinorin.qualtet.auth.ResponseToken
import net.yoshinorin.qualtet.http.errors.ResponseProblemDetails
import net.yoshinorin.qualtet.fixture.Fixture.{author, authorService, router, unsafeDecode}
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.v1.AuthRouteSpec
class AuthRouteV1Spec extends AnyWordSpec {

  val a: AuthorResponseModel = authorService.findByName(author.name).unsafeRunSync().get
  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "AuthRoute" should {

    "return JWT correctly" in {
      val json =
        s"""
          |{
          |  "authorId" : "${a.id.value}",
          |  "password" : "pass"
          |}
        """.stripMargin

      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/v1/token/", entity = entity))
        .use { response =>
          IO {
            assert(response.status === Ok)
            assert(response.contentType.get === `Content-Type`(MediaType.application.json))

            val maybeToken = unsafeDecode[ResponseToken](response)
            assert(maybeToken.token.count(_ === '.') === 2)
          }
        }
        .unsafeRunSync()
    }

    "reject with unauthorized (wrong JSON format)" in {
      val wrongJsonFormat =
        s"""
          |{
          |  "authorId" : "${a.id.value}"
          |  "password" : "valid-password"
          |}
        """.stripMargin

      val entity = EntityEncoder[IO, String].toEntity(wrongJsonFormat)
      client
        .run(Request(method = Method.POST, uri = uri"/v1/token/", entity = entity))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "reject with unauthorized (can not decode request JSON without password key)" in {
      val wrongJson =
        s"""
          |{
          |  "authorId" : "${a.id.value}"
          |}
        """.stripMargin

      val entity = EntityEncoder[IO, String].toEntity(wrongJson)
      client
        .run(Request(method = Method.POST, uri = uri"/v1/token/", entity = entity))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "reject with unauthorized (can not decode request JSON without authorId key)" in {
      val wrongJson =
        """
          |{
          |  "password" : "valid-password"
          |}
        """.stripMargin

      val entity = EntityEncoder[IO, String].toEntity(wrongJson)
      client
        .run(Request(method = Method.POST, uri = uri"/v1/token/", entity = entity))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "reject with wrong-password" in {
      val json =
        s"""
           |{
           |  "authorId" : "${a.id.value}",
           |  "password" : "wrong-pass"
           |}
        """.stripMargin

      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/v1/token/", entity = entity))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "return if user not exists" in {
      val json =
        s"""
           |{
           |  "authorId" : "not-exists-user",
           |  "password" : "pass"
           |}
        """.stripMargin

      val entity = EntityEncoder[IO, String].toEntity(json)
      client
        .run(Request(method = Method.POST, uri = uri"/v1/token/", entity = entity))
        .use { response =>
          IO {
            assert(response.status === NotFound)
            assert(response.contentType.get === `Content-Type`(MediaType.application.`problem+json`))
            // TODO: avoid to return user not found message
            val maybeError = unsafeDecode[ResponseProblemDetails](response)
            assert(maybeError.title === "Not Found")
            assert(maybeError.status === 404)
            assert(maybeError.detail === "not-exists-user is not found.")
            assert(maybeError.instance === "/v1/token/")
          }
        }
        .unsafeRunSync()
    }

    "return Method Not Allowed" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/v1/token"))
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
