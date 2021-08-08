package net.yoshinorin.qualtet.http

import net.yoshinorin.qualtet.auth.RequestToken
import net.yoshinorin.qualtet.domains.models.Fail.BadRequest
import net.yoshinorin.qualtet.domains.models.contents.RequestContent
import org.scalatest.wordspec.AnyWordSpec

import java.time.ZonedDateTime

// testOnly net.yoshinorin.qualtet.http.RequestDecoderSpec
class RequestDecoderSpec extends AnyWordSpec with RequestDecoder {

  "RequestDecoder" should {

    "Request content JSON can decode without has initial value field" in {
      val json =
        """
          |{
          |  "author" : "JhonDue",
          |  "contentType" : "article",
          |  "path" : "/test/path",
          |  "title" : "this is a title",
          |  "rawContent" : "this is a raw content"
          |}
        """.stripMargin

      decode[RequestContent](json) match {
        case Left(_) => // Nothing to do
        case Right(r) => {
          assert(r.isInstanceOf[RequestContent])
          assert(r.author == "JhonDue")
          assert(r.contentType == "article")
          assert(r.path == "/test/path")
          assert(r.title == "this is a title")
          assert(r.rawContent == "this is a raw content")
          assert(r.publishedAt <= ZonedDateTime.now.toEpochSecond)
          assert(r.updatedAt <= ZonedDateTime.now.toEpochSecond)
        }
      }
    }

    "Request content JSON can decode with initial value field" in {
      val json =
        """
          |{
          |  "author" : "JhonDue",
          |  "contentType" : "article",
          |  "path" : "/test/path",
          |  "title" : "this is a title",
          |  "rawContent" : "this is a raw content",
          |  "publishedAt" : 1537974000,
          |  "updatedAt" : 1621098091
          |}
        """.stripMargin

      decode[RequestContent](json) match {
        case Left(_) => // Nothing to do
        case Right(r) => {
          assert(r.isInstanceOf[RequestContent])
          assert(r.author == "JhonDue")
          assert(r.contentType == "article")
          assert(r.path == "/test/path")
          assert(r.title == "this is a title")
          assert(r.rawContent == "this is a raw content")
          assert(r.publishedAt == 1537974000)
          assert(r.updatedAt == 1621098091)
        }
      }
    }

    "Request content JSON can not decode" in {
      val json =
        """
          |{
          |  "author" : "JhonDue",
          |  "title" : "this is a title",
          |  "rawContent" : "this is a raw content"
          |}
        """.stripMargin

      decode[RequestContent](json) match {
        case Right(_) => // Nothig to do
        case Left(l) => assert(l.isInstanceOf[BadRequest])
      }
    }

    "Request token JSON can decode" in {
      val json =
        """
          |{
          |  "authorId" : "dbed0c8e-57b9-4224-af10-c2ee9b49c066",
          |  "password" : "password"
          |}
        """.stripMargin

      decode[RequestToken](json) match {
        case Left(_) => // Nothing to do
        case Right(r) => {
          assert(r.isInstanceOf[RequestToken])
          assert(r.authorId == "dbed0c8e-57b9-4224-af10-c2ee9b49c066")
          assert(r.password == "password")
        }
      }
    }

    "Request token JSON can not decode" in {
      val json =
        """
          |{
          |  "authorId" : "dbed0c8e-57b9-4224-af10-c2ee9b49c066"
          |}
        """.stripMargin

      decode[RequestToken](json) match {
        case Right(_) => // Nothig to do
        case Left(l) => assert(l.isInstanceOf[BadRequest])
      }
    }

  }

}
