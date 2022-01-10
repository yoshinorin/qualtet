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
import net.yoshinorin.qualtet.domains.models.contents.{Content, Path, RequestContent, ResponseContent}
import net.yoshinorin.qualtet.domains.models.externalResources.{ExternalResourceKind, ExternalResources}
import net.yoshinorin.qualtet.domains.models.robots.Attributes
import net.yoshinorin.qualtet.domains.services.{AuthorService, ContentService}
import net.yoshinorin.qualtet.fixture.Fixture.{authorId, authorId2, validBCryptPassword}
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
  when(mockAuthorService.findByIdWithPassword(authorId))
    .thenReturn(
      IO(
        Some(
          Author(
            id = authorId,
            name = AuthorName("JhonDue"),
            displayName = AuthorDisplayName("JD"),
            password = validBCryptPassword
          )
        )
      )
    )

  // Correct user
  when(mockAuthorService.findById(authorId))
    .thenReturn(
      IO(
        Some(
          ResponseAuthor(
            id = authorId,
            name = AuthorName("JhonDue"),
            displayName = AuthorDisplayName("JD")
          )
        )
      )
    )

  // user not found
  when(mockAuthorService.findByIdWithPassword(authorId2))
    .thenReturn(
      IO(
        Some(
          Author(
            id = authorId2,
            name = AuthorName("notfound"),
            displayName = AuthorDisplayName("NF"),
            password = validBCryptPassword
          )
        )
      )
    )

  // user not found
  when(mockAuthorService.findById(authorId2))
    .thenReturn(
      IO(None)
    )

  val authService = new AuthService(mockAuthorService, jwtInstance)
  val validToken: String = authService.generateToken(RequestToken(authorId, "pass")).unsafeRunSync().token
  val notFoundUserToken: String = authService.generateToken(RequestToken(authorId2, "pass")).unsafeRunSync().token
  val mockContentService: ContentService = Mockito.mock(classOf[ContentService])
  val contentRoute: ContentRoute = new ContentRoute(authService, mockContentService)

  // POST
  when(
    mockContentService.createContentFromRequest(
      AuthorName("JhonDue"),
      RequestContent(
        contentType = "article",
        path = Path("/test/path"),
        title = "this is a title",
        rawContent = "this is a raw content",
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = Option(List("Scala", "Akka")),
        externalResources = Option(
          List(
            ExternalResources(
              ExternalResourceKind("js"),
              values = List("test", "foo", "bar")
            )
          )
        )
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
    mockContentService.findByPathWithMeta(Path("/this/is/a/example/"))
  ).thenReturn(
    IO(
      Option(
        ResponseContent(
          title = "this is a title",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          content = "html content",
          publishedAt = 1567814290
        )
      )
    )
  )

  // GET
  when(
    mockContentService.findByPathWithMeta(Path("/this/is/a/404/"))
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
          |  "robotsAttributes" : "noarchive, noimageindex",
          |  "externalResources" : null,
          |  "tags" : null,
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
