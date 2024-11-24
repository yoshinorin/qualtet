package net.yoshinorin.qualtet.domains.contentTaggings

import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.domains.contents.RequestContent
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.series.*
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import org.scalatest.Ignore

// testOnly net.yoshinorin.qualtet.domains.contentTaggings.ContentSerializingServiceSpec
@Ignore // TODO: write testcode when implement delete feature
class ContentSerializingServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    val requestSeries: RequestSeries = RequestSeries(
      title = "Content Serializing Service Spec",
      name = SeriesName("content-serializing-service-spec"),
      None
    )
    createSeries(List(requestSeries))

    val requestContents: List[RequestContent] = {
      List(1, 5)
        .map(_.toString())
        .map(i =>
          RequestContent(
            contentType = "article",
            path = Path(s"/test/ContentSerializingServiceSpec-${i}"),
            title = s"this is a ContentSerializingServiceSpec title ${i}",
            rawContent = s"this is a ContentSerializingServiceSpec raw content ${i}",
            htmlContent = s"this is a ContentSerializingServiceSpec html content ${i}",
            robotsAttributes = Attributes("noarchive, noimageindex"),
            tags = List(s"ContentSerializingServiceSpec${i}.1", s"ContentSerializingServiceSpec${i}.2", s"ContentSerializingServiceSpec${i}.3"),
            series = Option(requestSeries.name),
            externalResources = List()
          )
        )
    }
    createContents(requestContents)
  }

  "ContentSerializingService" should {
    // TODO
  }

}
