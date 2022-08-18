package net.yoshinorin.qualtet.http.routes

import org.scalatest.wordspec.AnyWordSpec
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.fixture.Fixture.{authService, author, authorService, cacheRoute, expiredToken, nonExistsUserToken}
import net.yoshinorin.qualtet.auth.RequestToken
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.AuthenticationFailedRejection

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.routes.CacheRouteSpec
class CacheRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val validAuthor: ResponseAuthor = authorService.findByName(author.name).unsafeRunSync().get
  val validToken: String = authService.generateToken(RequestToken(validAuthor.id, "pass")).unsafeRunSync().token

  "CacheRoute" should {
    "be invalidate all caches" in {
      Delete("/caches/")
        .addCredentials(OAuth2BearerToken(validToken)) ~> cacheRoute.route ~> check {
        assert(status === StatusCodes.NoContent)
      }
    }

    "be reject caused by expired token" in {
      Delete("/caches/")
        .addCredentials(OAuth2BearerToken(expiredToken)) ~> cacheRoute.route ~> check {
        // TODO: fix status code
        assert(status === StatusCodes.InternalServerError)
      }
    }

    "be reject caused by the authorization header is empty" in {
      Delete("/caches/").withEntity(ContentTypes.`application/json`, """{}""") ~> cacheRoute.route ~> check {
        assert(rejection.asInstanceOf[AuthenticationFailedRejection].cause === AuthenticationFailedRejection.CredentialsMissing)
      }
    }

    "be reject caused by invalid token" in {
      Delete("/caches/")
        .addCredentials(OAuth2BearerToken("invalid token")) ~> cacheRoute.route ~> check {
        // TODO: fix status code
        assert(status === StatusCodes.InternalServerError)
      }
    }

    "be return user not found" in {
      Delete("/caches/")
        .addCredentials(OAuth2BearerToken(nonExistsUserToken)) ~> cacheRoute.route ~> check {
        // TODO: fix status code
        assert(status === StatusCodes.InternalServerError)
      }
    }

  }
}
