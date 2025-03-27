package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.domains.series.{SeriesName, SeriesRequestModel}
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.tags.SeriesServiceSpec
class SeriesServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  val seriesName = SeriesName("seriesservice-series")

  override protected def beforeAll(): Unit = {
    val requestSeries: List[SeriesRequestModel] = List(
      SeriesRequestModel(
        title = "Series Service Spec",
        name = seriesName,
        None
      )
    )

    requestSeries.unsafeCreateSeries()
    createContentRequestModels(5, "SeriesService", Some(requestSeries.head.name)).unsafeCreateConternt()
  }

  "SeriesService" should {
    "get all series" in {
      (for {
        result <- seriesService.getAll
      } yield {
        val filteredResult = result.filter(s => s.name.value.contains(seriesName.value))
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
  }

  "get" in {
    (for {
      result <- seriesService.get(seriesName)
    } yield {
      assert(result.title === "Series Service Spec")
      assert(result.name.value === seriesName.value)
      assert(result.articles.size === 5)
      assert(result.articles.head.path.value === "/test/SeriesService-0")
    }).unsafeRunSync()
  }

  "upsert" in {
    val seriesName = SeriesName("seriesservice-series-upsert")
    (for {
      created <- seriesService.create(
        SeriesRequestModel(
          title = "Series Service Spec Created",
          name = seriesName,
          description = Some("series description")
        )
      )
      // update series title
      updated <- seriesService.create(
        SeriesRequestModel(
          title = "Series Service Spec Updated",
          name = seriesName,
          description = Some("series description")
        )
      )
    } yield {
      assert(created.id === updated.id)
      assert(created.title != updated.title)
      assert(updated.name.value === seriesName.value)
      assert(updated.title === "Series Service Spec Updated")
      assert(created.description.get === "series description")
    }).unsafeRunSync()

  }
}
