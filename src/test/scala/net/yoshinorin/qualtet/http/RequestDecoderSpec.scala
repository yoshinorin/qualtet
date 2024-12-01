package net.yoshinorin.qualtet.http

import net.yoshinorin.qualtet.auth.RequestToken
import net.yoshinorin.qualtet.domains.authors.AuthorId
import net.yoshinorin.qualtet.domains.contents.RequestContent
import net.yoshinorin.qualtet.domains.errors.UnexpectedJsonFormat
import net.yoshinorin.qualtet.domains.robots.Attributes
import org.scalatest.wordspec.AnyWordSpec

import java.time.ZonedDateTime

// testOnly net.yoshinorin.qualtet.http.RequestDecoderSpec
class RequestDecoderSpec extends AnyWordSpec with RequestDecoder {

  "RequestDecoder" should {
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

      val result = decode[RequestContent](json)
      assert(result.isRight)
      result match {
        case Left(_) => // Nothing to do
        case Right(r) => {
          assert(r.isInstanceOf[RequestContent])
          assert(r.contentType === "article")
          assert(r.path.value === "/test/path")
          assert(r.title === "this is a title")
          assert(r.rawContent === "this is a raw content")
          assert(r.htmlContent === "this is a html content")
          assert(r.externalResources.isEmpty)
          assert(r.publishedAt <= ZonedDateTime.now.toEpochSecond)
          assert(r.updatedAt <= ZonedDateTime.now.toEpochSecond)
        }
      }
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

      val result = decode[RequestContent](json)
      assert(result.isRight)
      result match {
        case Left(_) => // Nothing to do
        case Right(r) => {
          assert(r.isInstanceOf[RequestContent])
          assert(r.contentType === "article")
          assert(r.path.value === "/test/path")
          assert(r.title === "this is a title")
          assert(r.rawContent === "this is a raw content")
          assert(r.htmlContent === "this is a html content")
          assert(r.publishedAt === 1537974000)
          assert(r.updatedAt === 1621098091)
        }
      }
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

      val result = decode[RequestContent](json)
      assert(result.isRight)
      result match {
        case Left(_) => // Nothing to do
        case Right(r) => {
          assert(r.isInstanceOf[RequestContent])
          assert(r.contentType === "article")
          assert(r.path.value === "/test/path")
          assert(r.title === "this is a title")
          assert(r.rawContent === "this is a raw content")
          assert(r.htmlContent === "this is a html content")
          assert(r.robotsAttributes === Attributes("noarchive, nofollow, noimageindex, noindex"))
          assert(r.publishedAt === 1537974000)
          assert(r.updatedAt === 1621098091)
        }
      }
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

      val result = decode[RequestContent](json)
      assert(result.isLeft)
      result match {
        case Right(_) => // Nothig to do
        case Left(l) => assert(l.isInstanceOf[UnexpectedJsonFormat])
      }
    }

    "Request token JSON can decode" in {
      val json =
        """
          |{
          |  "authorId" : "01febb8az5t42m2h68xj8c754a",
          |  "password" : "password"
          |}
        """.stripMargin

      val result = decode[RequestToken](json)
      assert(result.isRight)
      result match {
        case Left(_) => // Nothing to do
        case Right(r) => {
          assert(r.isInstanceOf[RequestToken])
          assert(r.authorId.isInstanceOf[AuthorId])
          assert(r.authorId.value === "01febb8az5t42m2h68xj8c754a")
          assert(r.password === "password")
        }
      }
    }

    "Request token JSON can not decode" in {
      val json =
        """
          |{
          |  "authorId" : "01febb8az5t42m2h68xj8c754a"
          |}
        """.stripMargin

      val result = decode[RequestToken](json)
      assert(result.isLeft)
      result match {
        case Right(_) => // Nothig to do
        case Left(l) => assert(l.isInstanceOf[UnexpectedJsonFormat])
      }
    }
  }

}
