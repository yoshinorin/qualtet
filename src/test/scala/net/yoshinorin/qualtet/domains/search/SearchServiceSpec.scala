package net.yoshinorin.qualtet.domains.search

import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.actions.Action._
import net.yoshinorin.qualtet.fixture.Fixture._
import net.yoshinorin.qualtet.Modules._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import net.yoshinorin.qualtet.message.Fail.UnprocessableEntity
import net.yoshinorin.qualtet.syntax._

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
    requestContents.foreach { rc => contentService.createContentFromRequest(AuthorName(author.name.value), rc).unsafeRunSync() }
  }

  "SearchService" should {

    "be extract query string" in {
      assert(searchService.validateAndExtractQueryString(Map(("q", List("abcde", "fghr", "jklmn")))) === List("abcde", "fghr", "jklmn"))
      assert(searchService.validateAndExtractQueryString(Map(("q", List("aaaa", "bbbb", "cccc")))) === List("aaaa", "bbbb", "cccc"))
    }

    "be return sarch result" in {
      val s = searchService.search(Map(("q", List("searchService")))).unsafeRunSync()
      assert(s.count === 54)
      assert(s.contents.size === 30)
    }

    "be return empty sarch result" in {
      val s = searchService.search(Map(("q", List("notfound")))).unsafeRunSync()
      assert(s.count === 0)
      assert(s.contents.size === 0)
    }

    /* TODO:
    "be filtered http url" in {
      val s = searchService.search(Map(("q", List("http")))).unsafeRunSync()
      assert(s.count === 1)
      assert(s.contents.size === 1)
    }

    "be filtered https url" in {
      val s = searchService.search(Map(("q", List("https")))).unsafeRunSync()
      assert(s.count === 0)
      assert(s.contents.size === 0)
    }
     */

    "be return `AND` sarch result" in {
      val s = searchService.search(Map(("q", List("searchService", "Last")))).unsafeRunSync()
      assert(s.count === 1)
      assert(s.contents.size === 1)
    }

    // TODO: in-casesensitive assertion

    "be throw UnprocessableEntity Exception if query string is empty" in {
      assertThrows[UnprocessableEntity] {
        searchService.validateAndExtractQueryString(Map(("", List())))
      }
    }

    "be throw UnprocessableEntity Exception if query value is empty" in {
      assertThrows[UnprocessableEntity] {
        searchService.validateAndExtractQueryString(Map(("q", List())))
      }
    }

    "be throw UnprocessableEntity Exception if query key is wrong" in {
      assertThrows[UnprocessableEntity] {
        searchService.validateAndExtractQueryString(Map(("wrong", List("abcde"))))
      }
    }

    "be throw UnprocessableEntity Exception if many query requested" in {
      assertThrows[UnprocessableEntity] {
        searchService.validateAndExtractQueryString(Map(("q", List("abcde", "abcde", "abcde", "abcde"))))
      }
    }

    "be throw UnprocessableEntity Exception if query contains too short value" in {
      assertThrows[UnprocessableEntity] {
        searchService.validateAndExtractQueryString(Map(("q", List("abcd", "abc", "abcd"))))
      }
    }

    "be throw UnprocessableEntity Exception if query contains invalid char" in {
      assertThrows[UnprocessableEntity] {
        searchService.validateAndExtractQueryString(Map(("q", List("abcd", "ab(d", "abcd"))))
      }
    }
  }

}
