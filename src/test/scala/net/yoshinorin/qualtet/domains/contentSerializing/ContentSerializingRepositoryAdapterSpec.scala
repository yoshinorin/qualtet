package net.yoshinorin.qualtet.domains.contentTaggings

import net.yoshinorin.qualtet.domains.contents.{ContentPath, ContentRequestModel}
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.series.*
import net.yoshinorin.qualtet.domains.tags.{Tag, TagName, TagPath}
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import org.scalatest.Ignore

// testOnly net.yoshinorin.qualtet.domains.contentTaggings.ContentSerializingRepositoryAdapterSpec
@Ignore // TODO: write testcode when implement delete feature
class ContentSerializingRepositoryAdapterSpec extends AnyWordSpec with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    val requestSeries: SeriesRequestModel = SeriesRequestModel(
      title = "Content Serializing Repository Adapter Spec",
      name = SeriesName("content-serializing-repository-adapter-spec"),
      None
    )
    List(requestSeries).unsafeCreateSeries()

    val requestContents: List[ContentRequestModel] = {
      List(1, 5)
        .map(_.toString())
        .map(i =>
          ContentRequestModel(
            contentType = "article",
            path = ContentPath(s"/test/ContentSerializingRepositoryAdapterSpec-${i}"),
            title = s"this is a ContentSerializingRepositoryAdapterSpec title ${i}",
            rawContent = s"this is a ContentSerializingRepositoryAdapterSpec raw content ${i}",
            htmlContent = s"this is a ContentSerializingRepositoryAdapterSpec html content ${i}",
            robotsAttributes = Attributes("noarchive, noimageindex"),
            tags = List(
              Tag(name = TagName(s"ContentSerializingRepositoryAdapterSpec${i}.1"), path = TagPath(s"ContentSerializingRepositoryAdapterSpec-path${i}.1")),
              Tag(name = TagName(s"ContentSerializingRepositoryAdapterSpec${i}.2"), path = TagPath(s"ContentSerializingRepositoryAdapterSpec-path${i}.2")),
              Tag(name = TagName(s"ContentSerializingRepositoryAdapterSpec${i}.3"), path = TagPath(s"ContentSerializingRepositoryAdapterSpec-path${i}.3"))
            ),
            series = Option(requestSeries.name),
            externalResources = List()
          )
        )
    }
    requestContents.unsafeCreateConternt()
  }

  "ContentSerializingRepositoryAdapterSpec" should {
    // TODO
  }

}
