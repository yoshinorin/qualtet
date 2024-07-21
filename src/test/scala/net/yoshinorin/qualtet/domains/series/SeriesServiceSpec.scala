package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.domains.contents.Path
import net.yoshinorin.qualtet.domains.series.{RequestSeries, SeriesName}
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.SeriesServiceSpec
class SeriesServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    val requestSeries: List[RequestSeries] = List(
      RequestSeries(
        title = "Series Service Spec",
        name = SeriesName("seriesservice-series"),
        None
      ),
      RequestSeries(
        title = "Series Service Spec2",
        name = SeriesName("seriesservice-series2"),
        None
      )
    )
    createSeries(requestSeries)

    val requestContents = makeRequestContents(5, "SeriesService", Some(requestSeries.head.name))
    createContents(requestContents)
  }

  "SeriesService" should {

    "get all series" in {
      val result = seriesService.getAll.unsafeRunSync().filter(s => s.name.value.contains("seriesservice-series"))

      // TODO: fix test data and assertion
      assert(result.size >= 2)
    }

    "findByName" in {
      val result = seriesService.findByName(SeriesName("seriesservice-series")).unsafeRunSync()

      assert(result.size === 1)
      assert(result.get.title === "Series Service Spec")
    }

    "get" in {
      val result = seriesService.get(SeriesName("seriesservice-series")).unsafeRunSync()

      assert(result.title === "Series Service Spec")
      assert(result.name.value === "seriesservice-series")
      assert(result.articles.size === 5)
      assert(result.articles.head.path.value === "/test/SeriesService-0")
    }

    "upsert" in {
      seriesService
        .create(
          RequestSeries(
            title = "Series Service Spec Created",
            name = SeriesName("seriesservice-series-created"),
            description = Some("series description")
          )
        )
        .unsafeRunSync()
      val resultAfterCreated = seriesService.findByName(SeriesName("seriesservice-series-created")).unsafeRunSync().get
      seriesService
        .create(
          RequestSeries(
            title = "Series Service Spec Updated",
            name = SeriesName("seriesservice-series-created"),
            description = Some("series description")
          )
        )
        .unsafeRunSync()
      val resultAfterUpdated = seriesService.findByName(SeriesName("seriesservice-series-created")).unsafeRunSync().get

      assert(resultAfterCreated.id === resultAfterUpdated.id)
      assert(resultAfterCreated.title != resultAfterUpdated.title)
      assert(resultAfterUpdated.name.value === "seriesservice-series-created")
      assert(resultAfterUpdated.title === "Series Service Spec Updated")
      assert(resultAfterCreated.description.get === "series description")
    }
  }

}
