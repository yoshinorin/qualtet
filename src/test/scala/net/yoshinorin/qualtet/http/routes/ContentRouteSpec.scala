package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.AuthenticationFailedRejection
import akka.http.scaladsl.server.AuthenticationFailedRejection.CredentialsMissing
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.IO
import net.yoshinorin.qualtet.auth.{AuthService, Jwt, KeyPair, RequestToken}
import net.yoshinorin.qualtet.domains.models.authors.{Author, AuthorDisplayName, AuthorId, AuthorName, BCryptPassword, ResponseAuthor}
import net.yoshinorin.qualtet.domains.models.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.models.contents.{Content, Path, RequestContent}
import net.yoshinorin.qualtet.domains.services.{AuthorService, ContentService}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.wordspec.AnyWordSpec
import pdi.jwt.JwtAlgorithm

import java.security.SecureRandom

// testOnly net.yoshinorin.qualtet.http.routes.ContentRouteSpec
class ContentRouteSpec extends AnyWordSpec with ScalatestRouteTest {

  val keyPair = new KeyPair("RSA", 2048, SecureRandom.getInstanceStrong)
  val message: Array[Byte] = SecureRandom.getInstanceStrong.toString.getBytes
  val signature = new net.yoshinorin.qualtet.auth.Signature("SHA256withRSA", message, keyPair)
  val jwtInstance = new Jwt(JwtAlgorithm.RS256, keyPair, signature)
  val mockAuthorService: AuthorService = Mockito.mock(classOf[AuthorService])

  // Correct user
  when(mockAuthorService.findByIdWithPassword(AuthorId("01FEBB8AZ5T42M2H68XJ8C754A")))
    .thenReturn(
      IO(
        Some(
          Author(
            id = AuthorId("01FEBB8AZ5T42M2H68XJ8C754A"),
            name = AuthorName("JhonDue"),
            displayName = AuthorDisplayName("JD"),
            password = BCryptPassword("$2a$10$XmRiVEV8yV9u8BnsIfSTTuzUvH/.6jutH6QvIX6zRoTcqkuKsxE0O")
          )
        )
      )
    )

  // Correct user
  when(mockAuthorService.findById(AuthorId("01FEBB8AZ5T42M2H68XJ8C754A")))
    .thenReturn(
      IO(
        Some(
          ResponseAuthor(
            id = AuthorId("01FEBB8AZ5T42M2H68XJ8C754A"),
            name = AuthorName("JhonDue"),
            displayName = AuthorDisplayName("JD")
          )
        )
      )
    )

  // user not found
  when(mockAuthorService.findByIdWithPassword(AuthorId("01FEBB8AZ5T42M2H68XJ8C754B")))
    .thenReturn(
      IO(
        Some(
          Author(
            id = AuthorId("01FEBB8AZ5T42M2H68XJ8C754B"),
            name = AuthorName("notfound"),
            displayName = AuthorDisplayName("NF"),
            password = BCryptPassword("$2a$10$XmRiVEV8yV9u8BnsIfSTTuzUvH/.6jutH6QvIX6zRoTcqkuKsxE0O")
          )
        )
      )
    )

  // user not found
  when(mockAuthorService.findById(AuthorId("01FEBB8AZ5T42M2H68XJ8C754B")))
    .thenReturn(
      IO(None)
    )

  val authService = new AuthService(mockAuthorService, jwtInstance)
  val validToken: String = authService.generateToken(RequestToken(AuthorId("01FEBB8AZ5T42M2H68XJ8C754A"), "pass")).unsafeRunSync().token
  val notFoundUserToken: String = authService.generateToken(RequestToken(AuthorId("01FEBB8AZ5T42M2H68XJ8C754B"), "pass")).unsafeRunSync().token
  val mockContentService: ContentService = Mockito.mock(classOf[ContentService])
  val contentRoute: ContentRoute = new ContentRoute(authService, mockContentService)

  // POST
  when(
    mockContentService.createContentFromRequest(
      RequestContent(
        authorName = AuthorName("JhonDue"),
        contentType = "article",
        path = Path("/test/path"),
        title = "this is a title",
        rawContent = "this is a raw content"
      )
    )
  ).thenReturn(
    IO(
      Content(
        authorId = new AuthorId,
        contentTypeId = new ContentTypeId,
        path = Path("/test/path/"),
        title = "this is a title",
        rawContent = "this is a raw content",
        htmlContent = "this is a html content"
      )
    )
  )

  // GET
  when(
    mockContentService.findByPath(Path("/this/is/a/example/"))
  ).thenReturn(
    IO(
      Option(
        Content(
          authorId = new AuthorId,
          contentTypeId = new ContentTypeId,
          path = Path("/this/is/a/example/"),
          title = "this is a title",
          rawContent = "raw content",
          htmlContent = "html content",
          publishedAt = 1567814290
        )
      )
    )
  )

  // GET
  when(
    mockContentService.findByPath(Path("/this/is/a/404/"))
  ).thenReturn(
    IO(None)
  )

  "ContentRoute" should {
    /*
    TODO: fix test case
    "success create content" in {
      val json =
        """
          |{
          |  "authorName" : "JhonDue",
          |  "contentType" : "article",
          |  "path" : "/test/path",
          |  "title" : "this is a title",
          |  "rawContent" : "this is a raw content"
          |}
        """.stripMargin

      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, json) ~> contentRoute.route ~> check {
        assert(status == StatusCodes.Created)
        assert(contentType == ContentTypes.`application/json`)
      }
    }
     */

    "reject with authorization header is empty" in {
      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, """{}""") ~> contentRoute.route ~> check {
        assert(rejection.asInstanceOf[AuthenticationFailedRejection].cause == CredentialsMissing)
      }
    }

    "reject with invalid token" in {
      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, """{}""") ~> addCredentials(OAuth2BearerToken("invalid token")) ~> contentRoute.route ~> check {
        // TODO: fix status code
        assert(status == StatusCodes.InternalServerError)
      }
    }

    "user not found" in {
      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, """{}""") ~> addCredentials(OAuth2BearerToken(notFoundUserToken)) ~> contentRoute.route ~> check {
        // TODO: fix status code
        assert(status == StatusCodes.InternalServerError)
      }
    }

    "reject with bad request (wrong JSON format)" in {
      val wrongJsonFormat =
        """
          |{
          |  "authorName" : "JhonDue",
          |  "contentType" : "article",
          |  "path" : "/test/path",
          |  "title" : "this is a title"
          |  "rawContent" : "this is a raw content"
          |}
        """.stripMargin

      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, wrongJsonFormat) ~> addCredentials(OAuth2BearerToken(validToken)) ~> contentRoute.route ~> check {
        assert(status == StatusCodes.BadRequest)
        // TODO: assert response json
      }
    }

    "reject with bad request (lack of JSON key)" in {
      val wrongJsonFormat =
        """
          |{
          |  "authorName" : "JhonDue",
          |  "contentType" : "article",
          |  "path" : "/test/path",
          |  "title" : "this is a title"
          |}
        """.stripMargin

      Post("/contents/")
        .withEntity(ContentTypes.`application/json`, wrongJsonFormat) ~> addCredentials(OAuth2BearerToken(validToken)) ~> contentRoute.route ~> check {
        assert(status == StatusCodes.BadRequest)
        // TODO: assert response json
      }
    }

    "return specific content" in {
      val expectJson =
        """
          |{
          |  "title" : "this is a title",
          |  "content" : "html content",
          |  "publishedAt" : 1567814290
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      Get("/contents/this/is/a/example/") ~> contentRoute.route ~> check {
        assert(status == StatusCodes.OK)
        assert(contentType == ContentTypes.`application/json`)
        assert(responseAs[String].replaceAll("\n", "").replaceAll(" ", "") == expectJson)
      }
    }

    "return 404" in {
      Get("/contents/this/is/a/404/") ~> contentRoute.route ~> check {
        assert(status == StatusCodes.NotFound)
        assert(contentType == ContentTypes.`application/json`)
      }
    }

  }

}
