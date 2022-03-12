package net.yoshinorin.qualtet.domains.services

import net.yoshinorin.qualtet.domains.models.authors.AuthorName
import net.yoshinorin.qualtet.domains.models.contents.{Path, RequestContent}
import net.yoshinorin.qualtet.domains.models.robots.Attributes
import net.yoshinorin.qualtet.fixture.Fixture._
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.services.ArchiveServiceSpec
class ArchiveServiceSpec extends AnyWordSpec {

  val requestContents: List[RequestContent] = {
    (1 until 40).toList.map(i =>
      RequestContent(
        contentType = "article",
        path = Path(s"/test/archives-${i}"),
        title = s"this is a archives title ${i}",
        rawContent = s"this is a archives raw content ${i}",
        htmlContent = Option(s"this is a archives html content ${i}"),
        robotsAttributes = Attributes("noarchive, noimageindex"),
        tags = Option(List(s"archivesTag${i}")),
        externalResources = Option(List())
      )
    )
  }

  // NOTE: create content and related data for test
  requestContents.foreach { rc => contentService.createContentFromRequest(AuthorName(author.name.value), rc).unsafeRunSync() }

  "ArchiveService" should {

    "be get" in {
      val result = archiveService.get.unsafeRunSync()
      assert(result.size >= 39)
    }

  }

}
