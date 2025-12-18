package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.fixture.unsafe
import net.yoshinorin.qualtet.domains.contents.ContentPath
import net.yoshinorin.qualtet.domains.series.{SeriesName, SeriesPath, SeriesRequestModel}
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.tags.SeriesServiceSpec
class SeriesServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  val seriesName = SeriesName("seriesservice-series-name")
  val seriesPath = SeriesPath("seriesservice-series-path").unsafe

  val upsertSeriesName = SeriesName("seriesservice-upsert-name")
  val upsertSeriesPath = SeriesPath("seriesservice-upsert-path").unsafe

  override protected def beforeAll(): Unit = {
    val requestSeries: List[SeriesRequestModel] = List(
      SeriesRequestModel(
        title = "Series Service Spec",
        name = seriesName,
        path = seriesPath,
        None
      ).unsafe,
      SeriesRequestModel(
        title = "Series Service Spec2",
        name = SeriesName("seriesservice-series2-name"),
        path = SeriesPath("seriesservice-series2-path").unsafe,
        None
      ).unsafe,
      SeriesRequestModel(
        title = "Series Service Spec Upsert",
        name = upsertSeriesName,
        path = upsertSeriesPath,
        None
      ).unsafe
    )

    requestSeries.unsafeCreateSeries()
    createContentRequestModels(5, "SeriesService", Some(requestSeries.head.name)).unsafeCreateConternt()
  }

  "SeriesService" should {
    "get all series" in {
      (for {
        result <- seriesService.getAll
      } yield {
        val filteredResult = result.filter(s => s.path.value.contains("seriesservice-series"))
        // TODO: fix test data and assertion
        assert(filteredResult.size >= 2)
      }).unsafeRunSync()
    }

    "findByName" in
      (for {
        result <- seriesService.findByName(seriesName)
      } yield {
        assert(result.size === 1)
        assert(result.get.title === "Series Service Spec")
      }).unsafeRunSync()

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
      getResult <- seriesService.get(seriesPath)
    } yield {
      val result = getResult.unsafe
      assert(result.title === "Series Service Spec")
      assert(result.name.value === seriesName.value)
      assert(result.path.value === seriesPath.value)
      assert(result.articles.size === 5)
      assert(result.articles.head.path.value === "/test/SeriesService-0")
    }).unsafeRunSync()
  }

  "upsert" in {
    (for {
      createResult <- seriesService.create(
        SeriesRequestModel(
          title = "Series Service Spec Created",
          name = upsertSeriesName,
          path = upsertSeriesPath,
          description = Some("series description")
        ).unsafe
      )
      created = createResult.unsafe
      // update series title
      updateResult <- seriesService.create(
        SeriesRequestModel(
          title = "Series Service Spec Updated",
          name = upsertSeriesName,
          path = upsertSeriesPath,
          description = Some("series description")
        ).unsafe
      )
      updated = updateResult.unsafe
    } yield {
      assert(created.id === updated.id)
      assert(created.title != updated.title)
      assert(updated.name.value === upsertSeriesName.value)
      assert(updated.path.value === upsertSeriesPath.value)
      assert(updated.title === "Series Service Spec Updated")
      assert(created.description.get === "series description")
    }).unsafeRunSync()

  }
}
