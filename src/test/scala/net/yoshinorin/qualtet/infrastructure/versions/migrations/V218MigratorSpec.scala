package net.yoshinorin.qualtet.infrastructure.versions.migrations

import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.tags.{TagId, TagName}
import net.yoshinorin.qualtet.domains.series.{SeriesId, SeriesName}
import net.yoshinorin.qualtet.infrastructure.versions.V218Migrator.*

// testOnly net.yoshinorin.qualtet.infrastructure.versions.migrations.V218MigratorSpec
class V218MigratorSpec extends AnyWordSpec {

  "convertTags" should {

    "return success flag as true for valid TagPath values" in {
      val tags = Seq(
        (1, TagUnsafeV218(TagId(), TagName("tag1"), "valid-path")),
        (2, TagUnsafeV218(TagId(), TagName("tag2"), "another-valid-path"))
      )

      val result = convertTags(tags)

      assert(result.length === 2)
      assert(result(0)._1.path === "valid-path")
      assert(result(0)._2 === true)
      assert(result(1)._1.path === "another-valid-path")
      assert(result(1)._2 === true)
    }

    "preserve original path and return failure flag as false for invalid TagPath values" in {
      val tags = Seq(
        (1, TagUnsafeV218(TagId(), TagName("tag1"), "invalid path with spaces")),
        (2, TagUnsafeV218(TagId(), TagName("tag2"), "invalid<>path"))
      )

      val result = convertTags(tags)

      assert(result.length === 2)
      assert(result(0)._1.path === "invalid path with spaces")
      assert(result(0)._2 === false)
      assert(result(1)._1.path === "invalid<>path")
      assert(result(1)._2 === false)
    }

    "properly handle mixed valid and invalid paths" in {
      val tags = Seq(
        (1, TagUnsafeV218(TagId(), TagName("tag1"), "valid-path")),
        (2, TagUnsafeV218(TagId(), TagName("tag2"), "invalid path")),
        (3, TagUnsafeV218(TagId(), TagName("tag3"), "another-valid"))
      )

      val result = convertTags(tags)

      assert(result.length === 3)
      assert(result(0)._1.path === "valid-path")
      assert(result(0)._2 === true)
      assert(result(1)._1.path === "invalid path")
      assert(result(1)._2 === false)
      assert(result(2)._1.path === "another-valid")
      assert(result(2)._2 === true)
    }

    "return empty result for empty input list" in {
      val tags = Seq.empty[(Int, TagUnsafeV218)]

      val result = convertTags(tags)

      assert(result.isEmpty)
    }

    "preserve tag ID and name without modification" in {
      val originalId = TagId("01ARZ3NDEKTSV4RRFFQ69G5FHV")
      val originalName = TagName("test-name")
      val tags = Seq(
        (1, TagUnsafeV218(originalId, originalName, "valid-path"))
      )

      val result = convertTags(tags)

      assert(result(0)._1.id === originalId)
      assert(result(0)._1.name === originalName)
    }

  }

  "aggregateConvertedTags" should {

    "return correct aggregation for mixed success and failure results" in {
      val convertedData = Seq(
        (TagUnsafeV218(TagId("01arz3ndektsv4rrffq69g5f1v"), TagName("name1"), "path1"), true),
        (TagUnsafeV218(TagId("01arz3ndektsv4rrffq69g5f2v"), TagName("name2"), "path2"), false),
        (TagUnsafeV218(TagId("01arz3ndektsv4rrffq69g5f3v"), TagName("name3"), "path3"), true),
        (TagUnsafeV218(TagId("01arz3ndektsv4rrffq69g5f4v"), TagName("name4"), "path4"), false)
      )

      val (convertedTags, successCount, failureCount, failedTags) = aggregateConvertedTags(convertedData)

      assert(convertedTags.length === 4)
      assert(successCount === 2)
      assert(failureCount === 2)
      assert(failedTags.length === 2)
      assert(convertedTags(0).id.value === "01arz3ndektsv4rrffq69g5f1v")
      assert(convertedTags(1).id.value === "01arz3ndektsv4rrffq69g5f2v")
      assert(convertedTags(2).id.value === "01arz3ndektsv4rrffq69g5f3v")
      assert(convertedTags(3).id.value === "01arz3ndektsv4rrffq69g5f4v")
      assert(failedTags(0).id.value === "01arz3ndektsv4rrffq69g5f2v")
      assert(failedTags(1).id.value === "01arz3ndektsv4rrffq69g5f4v")
    }

    "return all success counts when all conversions succeed" in {
      val convertedData = Seq(
        (TagUnsafeV218(TagId(), TagName("name1"), "path1"), true),
        (TagUnsafeV218(TagId(), TagName("name2"), "path2"), true)
      )

      val (convertedTags, successCount, failureCount, failedTags) = aggregateConvertedTags(convertedData)

      assert(convertedTags.length === 2)
      assert(successCount === 2)
      assert(failureCount === 0)
      assert(failedTags.isEmpty)
    }

    "return all failure counts when all conversions fail" in {
      val convertedData = Seq(
        (TagUnsafeV218(TagId(), TagName("name1"), "path1"), false),
        (TagUnsafeV218(TagId(), TagName("name2"), "path2"), false)
      )
      val (convertedTags, successCount, failureCount, failedTags) = aggregateConvertedTags(convertedData)

      assert(convertedTags.length === 2)
      assert(successCount === 0)
      assert(failureCount === 2)
      assert(failedTags.length === 2)
    }

    "return empty results for empty input" in {
      val convertedData = Seq.empty[(TagUnsafeV218, Boolean)]
      val (convertedTags, successCount, failureCount, failedTags) = aggregateConvertedTags(convertedData)

      assert(convertedTags.isEmpty)
      assert(successCount === 0)
      assert(failureCount === 0)
      assert(failedTags.isEmpty)
    }

  }

  "convertSeries" should {

    "return success flag as true for valid SeriesPath values" in {
      val series = Seq(
        (1, SeriesUnsafeV218(SeriesId(), SeriesName("series1"), "valid-path")),
        (2, SeriesUnsafeV218(SeriesId(), SeriesName("series2"), "another-valid-path"))
      )

      val result = convertSeries(series)

      assert(result.length === 2)
      assert(result(0)._1.path === "valid-path")
      assert(result(0)._2 === true)
      assert(result(1)._1.path === "another-valid-path")
      assert(result(1)._2 === true)
    }

    "preserve original path and return failure flag as false for invalid SeriesPath values" in {
      val series = Seq(
        (1, SeriesUnsafeV218(SeriesId(), SeriesName("series1"), "invalid path with spaces")),
        (2, SeriesUnsafeV218(SeriesId(), SeriesName("series2"), "invalid<>path"))
      )

      val result = convertSeries(series)

      assert(result.length === 2)
      assert(result(0)._1.path === "invalid path with spaces")
      assert(result(0)._2 === false)
      assert(result(1)._1.path === "invalid<>path")
      assert(result(1)._2 === false)
    }

    "properly handle mixed valid and invalid paths" in {
      val series = Seq(
        (1, SeriesUnsafeV218(SeriesId(), SeriesName("series1"), "valid-path")),
        (2, SeriesUnsafeV218(SeriesId(), SeriesName("series2"), "invalid path")),
        (3, SeriesUnsafeV218(SeriesId(), SeriesName("series3"), "another-valid"))
      )

      val result = convertSeries(series)

      assert(result.length === 3)
      assert(result(0)._1.path === "valid-path")
      assert(result(0)._2 === true)
      assert(result(1)._1.path === "invalid path")
      assert(result(1)._2 === false)
      assert(result(2)._1.path === "another-valid")
      assert(result(2)._2 === true)
    }

    "return empty result for empty input list" in {
      val series = Seq.empty[(Int, SeriesUnsafeV218)]

      val result = convertSeries(series)

      assert(result.isEmpty)
    }

    "preserve series ID and name without modification" in {
      val originalId = SeriesId()
      val originalName = SeriesName("test-series")
      val series = Seq(
        (1, SeriesUnsafeV218(originalId, originalName, "valid-path"))
      )

      val result = convertSeries(series)

      assert(result(0)._1.id === originalId)
      assert(result(0)._1.name === originalName)
    }

  }

  "aggregateConvertedSeries" should {

    "return correct aggregation for mixed success and failure results" in {
      val convertedData = Seq(
        (SeriesUnsafeV218(SeriesId("01arz3ndektsv4rrffq69g5f1v"), SeriesName("name1"), "path1"), true),
        (SeriesUnsafeV218(SeriesId("01arz3ndektsv4rrffq69g5f2v"), SeriesName("name2"), "path2"), false),
        (SeriesUnsafeV218(SeriesId("01arz3ndektsv4rrffq69g5f3v"), SeriesName("name3"), "path3"), true),
        (SeriesUnsafeV218(SeriesId("01arz3ndektsv4rrffq69g5f4v"), SeriesName("name4"), "path4"), false)
      )

      val (convertedSeries, successCount, failureCount, failedSeries) = aggregateConvertedSeries(convertedData)

      assert(convertedSeries.length === 4)
      assert(successCount === 2)
      assert(failureCount === 2)
      assert(failedSeries.length === 2)
      assert(convertedSeries(0).id.value === "01arz3ndektsv4rrffq69g5f1v")
      assert(convertedSeries(1).id.value === "01arz3ndektsv4rrffq69g5f2v")
      assert(convertedSeries(2).id.value === "01arz3ndektsv4rrffq69g5f3v")
      assert(convertedSeries(3).id.value === "01arz3ndektsv4rrffq69g5f4v")
      assert(failedSeries(0).id.value === "01arz3ndektsv4rrffq69g5f2v")
      assert(failedSeries(1).id.value === "01arz3ndektsv4rrffq69g5f4v")
    }

    "return all success counts when all conversions succeed" in {
      val convertedData = Seq(
        (SeriesUnsafeV218(SeriesId(), SeriesName("name1"), "path1"), true),
        (SeriesUnsafeV218(SeriesId(), SeriesName("name2"), "path2"), true)
      )

      val (convertedSeries, successCount, failureCount, failedSeries) = aggregateConvertedSeries(convertedData)

      assert(convertedSeries.length === 2)
      assert(successCount === 2)
      assert(failureCount === 0)
      assert(failedSeries.isEmpty)
    }

    "return all failure counts when all conversions fail" in {
      val convertedData = Seq(
        (SeriesUnsafeV218(SeriesId(), SeriesName("name1"), "path1"), false),
        (SeriesUnsafeV218(SeriesId(), SeriesName("name2"), "path2"), false)
      )

      val (convertedSeries, successCount, failureCount, failedSeries) = aggregateConvertedSeries(convertedData)

      assert(convertedSeries.length === 2)
      assert(successCount === 0)
      assert(failureCount === 2)
      assert(failedSeries.length === 2)
    }

    "return empty results for empty input" in {
      val convertedData = Seq.empty[(SeriesUnsafeV218, Boolean)]

      val (convertedSeries, successCount, failureCount, failedSeries) = aggregateConvertedSeries(convertedData)

      assert(convertedSeries.isEmpty)
      assert(successCount === 0)
      assert(failureCount === 0)
      assert(failedSeries.isEmpty)
    }

  }

}
