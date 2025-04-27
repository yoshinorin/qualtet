package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.domains.contents.ContentPath
import net.yoshinorin.qualtet.domains.series.{SeriesPath, SeriesRequestModel}
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.tags.SeriesServiceSpec
class SeriesServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  val seriesPath = SeriesPath("seriesservice-series")

  override protected def beforeAll(): Unit = {
    val requestSeries: List[SeriesRequestModel] = List(
      SeriesRequestModel(
        title = "Series Service Spec",
        path = seriesPath,
        None
      ),
      SeriesRequestModel(
        title = "Series Service Spec2",
        path = SeriesPath("seriesservice-series2"),
        None
      )
    )

    requestSeries.unsafeCreateSeries()
    createContentRequestModels(5, "SeriesService", Some(requestSeries.head.path)).unsafeCreateConternt()
  }

  "SeriesService" should {
    "get all series" in {
      (for {
        result <- seriesService.getAll
      } yield {
        val filteredResult = result.filter(s => s.path.value.contains(seriesPath.value))
        // TODO: fix test data and assertion
        assert(filteredResult.size >= 2)
      }).unsafeRunSync()
    }

    "findByPath" in
      (for {
        result <- seriesService.findByPath(seriesPath)
      } yield {
        assert(result.size === 1)
        assert(result.get.title === "Series Service Spec")
      }).unsafeRunSync()
  }

  "get" in {
    (for {
      result <- seriesService.get(seriesPath)
    } yield {
      assert(result.title === "Series Service Spec")
      assert(result.path.value === seriesPath.value)
      assert(result.articles.size === 5)
      assert(result.articles.head.path.value === "/test/SeriesService-0")
    }).unsafeRunSync()
  }

  "upsert" in {
    val seriesPath = SeriesPath("seriesservice-series-upsert")
    (for {
      created <- seriesService.create(
        SeriesRequestModel(
          title = "Series Service Spec Created",
          path = seriesPath,
          description = Some("series description")
        )
      )
      // update series title
      updated <- seriesService.create(
        SeriesRequestModel(
          title = "Series Service Spec Updated",
          path = seriesPath,
          description = Some("series description")
        )
      )
    } yield {
      assert(created.id === updated.id)
      assert(created.title != updated.title)
      assert(updated.path.value === seriesPath.value)
      assert(updated.title === "Series Service Spec Updated")
      assert(created.description.get === "series description")
    }).unsafeRunSync()

  }
}
