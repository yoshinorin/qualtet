package net.yoshinorin.qualtet.domains.search

import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.actions.Action._
import net.yoshinorin.qualtet.fixture.Fixture._
import net.yoshinorin.qualtet.Modules._
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.message.Fail.BadRequest
import net.yoshinorin.qualtet.syntax._

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.search.SearchServiceSpec
class SearchServiceSpec extends AnyWordSpec {

  val requestContents: List[RequestContent] = {
    (0 until 49).toList
      .map(_.toString())
      .map(i =>
        RequestContent(
          contentType = "article",
          path = Path(s"/test/searchService-${i}"),
          title = s"this is a searchService title ${i}",
          rawContent = s"this is a searchService raw content ${i}",
          htmlContent = s"this is a searchService html content ${i}",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          tags = List(s"searchService${i}"),
          externalResources = List()
        )
      ) :+ RequestContent(
      contentType = "article",
      path = Path(s"/test/searchServiceLast"),
      title = s"this is a searchService titleLast",
      rawContent = s"this is a searchService raw contentLast",
      htmlContent = s"this is a searchService html contentLast",
      robotsAttributes = Attributes("noarchive, noimageindex"),
      tags = List(s"searchServiceLast"),
      externalResources = List()
    )
  }

  // NOTE: create content and related data for test
  requestContents.foreach { rc => contentService.createContentFromRequest(AuthorName(author.name.value), rc).unsafeRunSync() }

  "SearchService" should {

    "be extract query string" in {
      assert(searchService.validateAndExtractQueryString(Map(("q", List("abcde", "fghr", "jklmn")))) === List("abcde", "fghr", "jklmn"))
      assert(searchService.validateAndExtractQueryString(Map(("q", List("aaaa", "bbbb", "cccc")))) === List("aaaa", "bbbb", "cccc"))
    }

    "be return sarch result" in {
      val s = searchService.search(Map(("q", List("searchService")))).unsafeRunSync()
      assert(s.count === 50)
      assert(s.contents.size === 30)
    }

    "be return empty sarch result" in {
      val s = searchService.search(Map(("q", List("notfound")))).unsafeRunSync()
      assert(s.count === 0)
      assert(s.contents.size === 0)
    }

    "be return `AND` sarch result" in {
      val s = searchService.search(Map(("q", List("searchService", "Last")))).unsafeRunSync()
      assert(s.count === 1)
      assert(s.contents.size === 1)
    }

    // TODO: in-casesensitive assertion

    "be throw BadRequest Exception if query string is empty" in {
      assertThrows[BadRequest] {
        searchService.validateAndExtractQueryString(Map(("", List())))
      }
    }

    "be throw BadRequest Exception if query value is empty" in {
      assertThrows[BadRequest] {
        searchService.validateAndExtractQueryString(Map(("q", List())))
      }
    }

    "be throw BadRequest Exception if query key is wrong" in {
      assertThrows[BadRequest] {
        searchService.validateAndExtractQueryString(Map(("wrong", List("abcde"))))
      }
    }

    "be throw BadRequest Exception if many query requested" in {
      assertThrows[BadRequest] {
        searchService.validateAndExtractQueryString(Map(("q", List("abcde", "abcde", "abcde", "abcde"))))
      }
    }

    "be throw BadRequest Exception if query contains too short value" in {
      assertThrows[BadRequest] {
        searchService.validateAndExtractQueryString(Map(("q", List("abcd", "abc", "abcd"))))
      }
    }

    "be throw BadRequest Exception if query contains invalid char" in {
      assertThrows[BadRequest] {
        searchService.validateAndExtractQueryString(Map(("q", List("abcd", "ab(d", "abcd"))))
      }
    }
  }

}
