package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.typelevel.ci.*
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.Modules.*
import net.yoshinorin.qualtet.fixture.Fixture.{author, expiredToken, nonExistsUserToken, router}
import net.yoshinorin.qualtet.auth.RequestToken

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.v1.CacheRouteSpec
class CacheRouteSpec extends AnyWordSpec {

  val validAuthor: ResponseAuthor = authorService.findByName(author.name).unsafeRunSync().get
  val validToken: String = authService.generateToken(RequestToken(validAuthor.id, "pass")).unsafeRunSync().token
  val client: Client[IO] = Client.fromHttpApp(router.routes.orNotFound)

  "CacheRoute" should {
    "be invalidate all caches" in {

      client
        .run(Request(method = Method.DELETE, uri = uri"/v1/caches/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))))
        .use { response =>
          IO {
            assert(response.status === NoContent)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "be reject caused by expired token" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/v1/caches/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + expiredToken))))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "be reject caused by the authorization header is empty" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/v1/caches/"))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "be reject caused by invalid token" ignore {
      client
        .run(Request(method = Method.DELETE, uri = uri"/v1/caches/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + "invalidToken"))))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "be return user not found" in {
      client
        .run(Request(method = Method.DELETE, uri = uri"/v1/caches/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + nonExistsUserToken))))
        .use { response =>
          IO {
            assert(response.status === Unauthorized)
            assert(response.contentType.isEmpty)
          }
        }
        .unsafeRunSync()
    }

    "be return Method Not Allowed" in {
      client
        .run(Request(method = Method.POST, uri = uri"/v1/caches", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))))
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
