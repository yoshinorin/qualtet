package net.yoshinorin.qualtet.http.request

import cats.effect.IO
import net.yoshinorin.qualtet.auth.RequestToken
import net.yoshinorin.qualtet.domains.authors.AuthorId
import net.yoshinorin.qualtet.domains.contents.ContentRequestModel
import net.yoshinorin.qualtet.domains.errors.UnexpectedJsonFormat
import net.yoshinorin.qualtet.domains.robots.Attributes
import org.scalatest.wordspec.AnyWordSpec

import java.time.ZonedDateTime
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.http.request.RequestDecoderSpec
class RequestDecoderSpec extends AnyWordSpec with Decoder {

  "Decoder" should {
    "Request content JSON can decode without has initial value field" in {
      val json =
        """
          |{
          |  "contentType" : "article",
          |  "path" : "/test/path",
          |  "title" : "this is a title",
          |  "rawContent" : "this is a raw content",
          |  "htmlContent" : "this is a html content",
          |  "robotsAttributes" : "noindex, noarchive, noimageindex, nofollow"
          |}
        """.stripMargin

      (for {
        decoded <- decode[IO, ContentRequestModel](json)
      } yield {
        assert(decoded.isRight)
        decoded.map { c =>
          assert(c.isInstanceOf[ContentRequestModel])
          assert(c.contentType === "article")
          assert(c.path.value === "/test/path")
          assert(c.title === "this is a title")
          assert(c.rawContent === "this is a raw content")
          assert(c.htmlContent === "this is a html content")
          assert(c.externalResources.isEmpty)
          assert(c.publishedAt <= ZonedDateTime.now.toEpochSecond)
          assert(c.updatedAt <= ZonedDateTime.now.toEpochSecond)
        }
      }).unsafeRunSync()
    }

    "Request content JSON can decode with initial value field" in {
      val json =
        """
          |{
          |  "contentType" : "article",
          |  "path" : "/test/path",
          |  "title" : "this is a title",
          |  "rawContent" : "this is a raw content",
          |  "htmlContent" : "this is a html content",
          |  "robotsAttributes" : "noindex, noarchive, noimageindex, nofollow",
          |  "publishedAt" : 1537974000,
          |  "updatedAt" : 1621098091
          |}
        """.stripMargin

      (for {
        decoded <- decode[IO, ContentRequestModel](json)
      } yield {
        assert(decoded.isRight)
        decoded.map { c =>
          assert(c.isInstanceOf[ContentRequestModel])
          assert(c.contentType === "article")
          assert(c.path.value === "/test/path")
          assert(c.title === "this is a title")
          assert(c.rawContent === "this is a raw content")
          assert(c.htmlContent === "this is a html content")
          assert(c.publishedAt === 1537974000)
          assert(c.updatedAt === 1621098091)
        }
      }).unsafeRunSync()
    }

    "Request content JSON can decode with htmlContent field" in {
      val json =
        """
          |{
          |  "contentType" : "article",
          |  "path" : "/test/path",
          |  "title" : "this is a title",
          |  "rawContent" : "this is a raw content",
          |  "htmlContent" : "this is a html content",
          |  "robotsAttributes" : "noindex, noarchive, noimageindex, nofollow",
          |  "externalResources":[{"kind":"js","values":["js1","js2"]},{"kind":"css","values":["css1","css2"]}],
          |  "publishedAt" : 1537974000,
          |  "updatedAt" : 1621098091
          |}
        """.stripMargin

      (for {
        decoded <- decode[IO, ContentRequestModel](json)
      } yield {
        assert(decoded.isRight)
        decoded.map { c =>
          assert(c.isInstanceOf[ContentRequestModel])
          assert(c.contentType === "article")
          assert(c.path.value === "/test/path")
          assert(c.title === "this is a title")
          assert(c.rawContent === "this is a raw content")
          assert(c.htmlContent === "this is a html content")
          assert(c.robotsAttributes === Attributes("noarchive, nofollow, noimageindex, noindex"))
          assert(c.publishedAt === 1537974000)
          assert(c.updatedAt === 1621098091)
        }
      }).unsafeRunSync()
    }

    "Request content JSON can not decode" in {
      val json =
        """
          |{
          |  "authorName" : "JhonDue",
          |  "title" : "this is a title",
          |  "rawContent" : "this is a raw content"
          |}
        """.stripMargin

      (for {
        decoded <- decode[IO, ContentRequestModel](json)
      } yield {
        assert(decoded.isLeft)
        decoded.swap.map { c =>
          assert(c.isInstanceOf[UnexpectedJsonFormat])
        }
      }).unsafeRunSync()
    }

    "Request token JSON can decode" in {
      val json =
        """
          |{
          |  "authorId" : "01febb8az5t42m2h68xj8c754a",
          |  "password" : "password"
          |}
        """.stripMargin

      (for {
        decoded <- decode[IO, RequestToken](json)
      } yield {
        assert(decoded.isRight)
        decoded.map { c =>
          assert(c.isInstanceOf[RequestToken])
          assert(c.authorId.isInstanceOf[AuthorId])
          assert(c.authorId.value === "01febb8az5t42m2h68xj8c754a")
          assert(c.password === "password")
        }
      }).unsafeRunSync()
    }

    "Request token JSON can not decode" in {
      val json =
        """
          |{
          |  "authorId" : "01febb8az5t42m2h68xj8c754a"
          |}
        """.stripMargin

      (for {
        decoded <- decode[IO, RequestToken](json)
      } yield {
        assert(decoded.isLeft)
        decoded.swap.map { c =>
          assert(c.isInstanceOf[UnexpectedJsonFormat])
        }
      }).unsafeRunSync()
    }
  }

}
