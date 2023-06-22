package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import org.http4s.client.Client
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.typelevel.ci._
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.Modules._
import net.yoshinorin.qualtet.fixture.Fixture.{author, cacheRoute, expiredToken, nonExistsUserToken, router}
import net.yoshinorin.qualtet.auth.RequestToken

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.CacheRouteSpec
class CacheRouteSpec extends AnyWordSpec {

  val validAuthor: ResponseAuthor = authorService.findByName(author.name).unsafeRunSync().get
  val validToken: String = authService.generateToken(RequestToken(validAuthor.id, "pass")).unsafeRunSync().token
  val client: Client[IO] = Client.fromHttpApp(router.routes)

  "CacheRoute" should {
    "be invalidate all caches" in {

      client
        .run(Request(method = Method.DELETE, uri = uri"/caches/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))))
        .use { response =>
          IO {
            assert(response.status === NoContent)
          }
        }
        .unsafeRunSync()
    }

    "be reject caused by expired token" ignore {
      client
        .run(Request(method = Method.DELETE, uri = uri"/caches/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + expiredToken))))
        .use { response =>
          IO {
            // TODO: assert(response.status === Unauthorized)
          }
        }
      // TODO: .unsafeRunSync()
    }

    "be reject caused by the authorization header is empty" ignore {
      client
        .run(Request(method = Method.DELETE, uri = uri"/caches/"))
        .use { response =>
          IO {
            // TODO: assert(response.status === Unauthorized)
          }
        }
      // TODO: .unsafeRunSync()
    }

    "be reject caused by invalid token" ignore {
      client
        .run(Request(method = Method.DELETE, uri = uri"/caches/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + "invalidToken"))))
        .use { response =>
          IO {
            // TODO: assert(response.status === Unauthorized)
          }
        }
      // TODO: .unsafeRunSync()
    }

    "be return user not found" ignore {
      client
        .run(Request(method = Method.DELETE, uri = uri"/caches/", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + nonExistsUserToken))))
        .use { response =>
          IO {
            // TODO: assert(response.status === Unauthorized)
          }
        }
      // TODO: .unsafeRunSync()
    }

    "be return Method Not Allowed" in {
      client
        .run(Request(method = Method.POST, uri = uri"/caches", headers = Headers(Header.Raw(ci"Authorization", "Bearer " + validToken))))
        .use { response =>
          IO {
            assert(response.status === MethodNotAllowed)
          }
        }
        .unsafeRunSync()
    }

  }
}
