package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.Modules._
import net.yoshinorin.qualtet.fixture.Fixture._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.ArchiveServiceSpec
class ArchiveServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  val requestContents: List[RequestContent] = {
    (1 until 40).toList
      .map(_.toString())
      .map(i =>
        RequestContent(
          contentType = "article",
          path = Path(s"/test/archives-${i}"),
          title = s"this is a archives title ${i}",
          rawContent = s"this is a archives raw content ${i}",
          htmlContent = s"this is a archives html content ${i}",
          robotsAttributes = Attributes("noarchive, noimageindex"),
          tags = List(s"archivesTag${i}"),
          externalResources = List()
        )
      )
  }

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContents(requestContents)
  }

  "ArchiveService" should {

    "be get" in {
      val result = archiveService.get.unsafeRunSync()
      assert(result.size >= 39)
    }

  }

}
