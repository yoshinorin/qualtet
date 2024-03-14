package net.yoshinorin.qualtet.domains.search

import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.Modules.*
import net.yoshinorin.qualtet.message.{Error => Err}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import net.yoshinorin.qualtet.message.Fail.UnprocessableEntity

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.search.SearchServiceSpec
class SearchServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

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
    ) :+ RequestContent(
      contentType = "article",
      path = Path(s"/test/searchServiceIncludesUrl1"),
      title = s"this is a searchService IncludesUrl1",
      rawContent = s"this is a searchService raw contentIncludesUrl1 https://example.com aaabbbccc",
      htmlContent = s"this is a searchService html contentIncludesUrl1 https://example.com aaabbbccc",
      robotsAttributes = Attributes("noarchive, noimageindex"),
      tags = List(s"IncludesUrl1"),
      externalResources = List()
    ) :+ RequestContent(
      contentType = "article",
      path = Path(s"/test/searchServiceIncludesUrl2"),
      title = s"this is a searchService IncludesUrl2",
      rawContent = s"this is a searchService raw contentIncludesUrl2 http://example.com aaabbbccc",
      htmlContent = s"this is a searchService html contentIncludesUrl2 http://example.com aaabbbccc",
      robotsAttributes = Attributes("noarchive, noimageindex"),
      tags = List(s"IncludesUrl2"),
      externalResources = List()
    ) :+ RequestContent(
      contentType = "article",
      path = Path(s"/test/searchServiceIncludesWrongUrl"),
      title = s"this is a searchService IncludesWrongUrl1",
      rawContent = s"this is a searchService raw contentIncludesWrongUrl1 htt://example.com aaabbbccc",
      htmlContent = s"this is a searchService html contentIncludesWrongUrl1 htt://example.com aaabbbccc",
      robotsAttributes = Attributes("noarchive, noimageindex"),
      tags = List(s"IncludesWrongUrl1"),
      externalResources = List()
    ) :+ RequestContent(
      contentType = "article",
      path = Path(s"/test/searchServiceIncludesHttpString"),
      title = s"this is a searchService IncludesHttpString",
      rawContent = s"this is a searchService raw contentIncludesHttp String http://example.com aaabbbccc http",
      htmlContent = s"this is a searchService html contentIncludesHttp String http://example.com aaabbbccc http",
      robotsAttributes = Attributes("noarchive, noimageindex"),
      tags = List(s"IncludesHttpString"),
      externalResources = List()
    )
  }

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContents(requestContents)
  }

  "SearchService" should {

    "extract query string" in {
      assert(searchService.extractQueryStringsFromQuery(Map(("q", List("abcde", "fghr", "jklmn")))) === List("abcde", "fghr", "jklmn"))
      assert(searchService.extractQueryStringsFromQuery(Map(("q", List("aaaa", "bbbb", "cccc")))) === List("aaaa", "bbbb", "cccc"))
      // NOTE: result should be lowercase
      assert(searchService.extractQueryStringsFromQuery(Map(("q", List("AAAA", "BBBB", "CCCC")))) === List("aaaa", "bbbb", "cccc"))
    }

    "not extract with wrong query key" in {
      assert(searchService.extractQueryStringsFromQuery(Map(("wrong", List("AAAA", "BBBB", "CCCC")))) === List())
    }

    "return query string is empty error" in {
      val result = searchService.accumurateQueryStringsErrors(
        List(
        )
      )
      assert(
        result === List(Err(message = "Search query required.", code = "SEARCH_QUERY_REQUIRED"))
      )
    }

    "return accumrated errors" in {
      val result = searchService.accumurateQueryStringsErrors(
        List(
          "a",
          "abc",
          "d.",
          "1234567890123456",
          "aaaa",
          "bbbb",
          "cccc"
        )
      )
      assert(
        result === List(
          Err(message = "a is too short. You must be more than 4 chars in one word.", code = "SEARCH_CHAR_LENGTH_TOO_SHORT"),
          Err(message = "abc is too short. You must be more than 4 chars in one word.", code = "SEARCH_CHAR_LENGTH_TOO_SHORT"),
          Err(message = "Contains unusable chars in d.", code = "INVALID_CHARS_INCLUDED"),
          Err(message = "d. is too short. You must be more than 4 chars in one word.", code = "SEARCH_CHAR_LENGTH_TOO_SHORT"),
          Err(message = "Contains unusable chars in 1234567890123456", code = "INVALID_CHARS_INCLUDED"),
          Err(message = "1234567890123456 is too long. You must be less than 15 chars in one word.", code = "SEARCH_CHAR_LENGTH_TOO_LONG"),
          Err(message = "Search words must be less than 3. You specified 7.", code = "TOO_MANY_SEARCH_WORDS")
        )
      )
    }

    "return sarch result" in {
      val s = searchService.search(Map(("q", List("searchService")))).unsafeRunSync()
      assert(s.count === 54)
      assert(s.contents.size === 30)
    }

    "return empty sarch result" in {
      val s = searchService.search(Map(("q", List("notfound")))).unsafeRunSync()
      assert(s.count === 0)
      assert(s.contents.size === 0)
    }

    /* TODO:
    "filtered http url" in {
      val s = searchService.search(Map(("q", List("http")))).unsafeRunSync()
      assert(s.count === 1)
      assert(s.contents.size === 1)
    }

    "filtered https url" in {
      val s = searchService.search(Map(("q", List("https")))).unsafeRunSync()
      assert(s.count === 0)
      assert(s.contents.size === 0)
    }
     */

    "return `AND` sarch result" in {
      val s = searchService.search(Map(("q", List("searchService", "Last")))).unsafeRunSync()
      assert(s.count === 1)
      assert(s.contents.size === 1)
    }

    "throw UnprocessableEntity Exception if query string is empty" in {
      assertThrows[UnprocessableEntity] {
        searchService.search(Map()).unsafeRunSync()
      }
    }

    "throw UnprocessableEntity Exception if query value is empty" in {
      assertThrows[UnprocessableEntity] {
        searchService.search(Map(("q", List()))).unsafeRunSync()
      }
    }

    "throw UnprocessableEntity Exception if many query requested" in {
      assertThrows[UnprocessableEntity] {
        searchService.search(Map(("q", List("abcde", "abcde", "abcde", "abcde")))).unsafeRunSync()
      }
    }

    "throw UnprocessableEntity Exception if query contains too short value" in {
      assertThrows[UnprocessableEntity] {
        searchService.search(Map(("q", List("abcd", "abc", "abcd")))).unsafeRunSync()
      }
    }

    "throw UnprocessableEntity Exception if query contains invalid char" in {
      assertThrows[UnprocessableEntity] {
        searchService.search(Map(("q", List("abcd", "ab(d", "abcd")))).unsafeRunSync()
      }
    }
  }

}
