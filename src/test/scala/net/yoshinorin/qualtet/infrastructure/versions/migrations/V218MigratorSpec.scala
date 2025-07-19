package net.yoshinorin.qualtet.infrastructure.versions.migrations

import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.tags.{TagId, TagName}
import net.yoshinorin.qualtet.domains.series.{SeriesId, SeriesName}
import net.yoshinorin.qualtet.infrastructure.versions.V218Migrator.*
import net.yoshinorin.qualtet.fixture.Fixture.log4catsLogger
import cats.Monad
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import net.yoshinorin.qualtet.infrastructure.db.Executer
import doobie.ConnectionIO
import doobie.implicits.*
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.*

// testOnly net.yoshinorin.qualtet.infrastructure.versions.migrations.V218MigratorSpec
class V218MigratorSpec extends AnyWordSpec {

  val logger = log4catsLogger.getLogger

  "convertTags" should {

    "return success flag as true for valid TagPath values" in {
      val tags = Seq(
        (TagUnsafeV218(TagId(), TagName("tag1"), "valid-path")),
        (TagUnsafeV218(TagId(), TagName("tag2"), "another-valid-path"))
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
        (TagUnsafeV218(TagId(), TagName("tag1"), "invalid path with spaces")),
        (TagUnsafeV218(TagId(), TagName("tag2"), "invalid<>path"))
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
        (TagUnsafeV218(TagId(), TagName("tag1"), "valid-path")),
        (TagUnsafeV218(TagId(), TagName("tag2"), "invalid path")),
        (TagUnsafeV218(TagId(), TagName("tag3"), "another-valid"))
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
      val tags = Seq.empty[TagUnsafeV218]
      val result = convertTags(tags)

      assert(result.isEmpty)
    }

    "preserve tag ID and name without modification" in {
      val originalId = TagId("01ARZ3NDEKTSV4RRFFQ69G5FHV")
      val originalName = TagName("test-name")
      val tags = Seq(
        (TagUnsafeV218(originalId, originalName, "valid-path"))
      )

      val result = convertTags(tags)

      assert(result(0)._1.id === originalId)
      assert(result(0)._1.name === originalName)
    }

  }

  "aggregateConverted for Tags" should {

    "return correct aggregation for mixed success and failure results" in {
      val convertedData = Seq(
        (TagUnsafeV218(TagId("01arz3ndektsv4rrffq69g5f1v"), TagName("name1"), "path1"), true),
        (TagUnsafeV218(TagId("01arz3ndektsv4rrffq69g5f2v"), TagName("name2"), "path2"), false),
        (TagUnsafeV218(TagId("01arz3ndektsv4rrffq69g5f3v"), TagName("name3"), "path3"), true),
        (TagUnsafeV218(TagId("01arz3ndektsv4rrffq69g5f4v"), TagName("name4"), "path4"), false)
      )

      val (convertedTags, successCount, failureCount, failedTags) = aggregateConverted(convertedData)

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

      val (convertedTags, successCount, failureCount, failedTags) = aggregateConverted(convertedData)

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
      val (convertedTags, successCount, failureCount, failedTags) = aggregateConverted(convertedData)

      assert(convertedTags.length === 2)
      assert(successCount === 0)
      assert(failureCount === 2)
      assert(failedTags.length === 2)
    }

    "return empty results for empty input" in {
      val convertedData = Seq.empty[(TagUnsafeV218, Boolean)]
      val (convertedTags, successCount, failureCount, failedTags) = aggregateConverted(convertedData)

      assert(convertedTags.isEmpty)
      assert(successCount === 0)
      assert(failureCount === 0)
      assert(failedTags.isEmpty)
    }

  }

  "convertSeries" should {

    "return success flag as true for valid SeriesPath values" in {
      val series = Seq(
        (SeriesUnsafeV218(SeriesId(), SeriesName("series1"), "valid-path")),
        (SeriesUnsafeV218(SeriesId(), SeriesName("series2"), "another-valid-path"))
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
        (SeriesUnsafeV218(SeriesId(), SeriesName("series1"), "invalid path with spaces")),
        (SeriesUnsafeV218(SeriesId(), SeriesName("series2"), "invalid<>path"))
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
        (SeriesUnsafeV218(SeriesId(), SeriesName("series1"), "valid-path")),
        (SeriesUnsafeV218(SeriesId(), SeriesName("series2"), "invalid path")),
        (SeriesUnsafeV218(SeriesId(), SeriesName("series3"), "another-valid"))
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
      val series = Seq.empty[SeriesUnsafeV218]
      val result = convertSeries(series)

      assert(result.isEmpty)
    }

    "preserve series ID and name without modification" in {
      val originalId = SeriesId()
      val originalName = SeriesName("test-series")
      val series = Seq(
        (SeriesUnsafeV218(originalId, originalName, "valid-path"))
      )

      val result = convertSeries(series)

      assert(result(0)._1.id === originalId)
      assert(result(0)._1.name === originalName)
    }

  }

  "aggregateConverted for Series" should {

    "return correct aggregation for mixed success and failure results" in {
      val convertedData = Seq(
        (SeriesUnsafeV218(SeriesId("01arz3ndektsv4rrffq69g5f1v"), SeriesName("name1"), "path1"), true),
        (SeriesUnsafeV218(SeriesId("01arz3ndektsv4rrffq69g5f2v"), SeriesName("name2"), "path2"), false),
        (SeriesUnsafeV218(SeriesId("01arz3ndektsv4rrffq69g5f3v"), SeriesName("name3"), "path3"), true),
        (SeriesUnsafeV218(SeriesId("01arz3ndektsv4rrffq69g5f4v"), SeriesName("name4"), "path4"), false)
      )

      val (convertedSeries, successCount, failureCount, failedSeries) = aggregateConverted(convertedData)

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

      val (convertedSeries, successCount, failureCount, failedSeries) = aggregateConverted(convertedData)

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

      val (convertedSeries, successCount, failureCount, failedSeries) = aggregateConverted(convertedData)

      assert(convertedSeries.length === 2)
      assert(successCount === 0)
      assert(failureCount === 2)
      assert(failedSeries.length === 2)
    }

    "return empty results for empty input" in {
      val convertedData = Seq.empty[(SeriesUnsafeV218, Boolean)]

      val (convertedSeries, successCount, failureCount, failedSeries) = aggregateConverted(convertedData)

      assert(convertedSeries.isEmpty)
      assert(successCount === 0)
      assert(failureCount === 0)
      assert(failedSeries.isEmpty)
    }

  }

  "runTagMigration" should {

    "successfully migrate tags with valid paths" in {
      val mockTagRepository = mock(classOf[TagRepositoryV217[ConnectionIO]])
      val mockExecuter = mock(classOf[Executer[ConnectionIO, IO]])
      val testTags = Seq(
        (TagUnsafeV218(TagId("01arz3ndektsv4rrffq69g5f1v"), TagName("tag1"), "valid-path")),
        (TagUnsafeV218(TagId("01arz3ndektsv4rrffq69g5f2v"), TagName("tag2"), "another-valid"))
      )

      when(mockTagRepository.getAll()).thenReturn(Monad[ConnectionIO].pure(testTags))
      when(mockTagRepository.bulkUpsert(any[List[TagUnsafeV218]])).thenReturn(Monad[ConnectionIO].pure(2))
      when(mockExecuter.transact(any[ConnectionIO[Any]])).thenReturn(
        IO.pure(testTags.asInstanceOf[Any]),
        IO.pure(2.asInstanceOf[Any])
      )

      runTagMigration(mockTagRepository, logger, mockExecuter).unsafeRunSync()

      verify(mockTagRepository).getAll()
      verify(mockTagRepository).bulkUpsert(any[List[TagUnsafeV218]])
      verify(mockExecuter, times(2)).transact(any[ConnectionIO[Any]])
    }

    "handle tags with invalid paths" in {
      val mockTagRepository = mock(classOf[TagRepositoryV217[ConnectionIO]])
      val mockExecuter = mock(classOf[Executer[ConnectionIO, IO]])
      val testTags = Seq(
        (TagUnsafeV218(TagId("01arz3ndektsv4rrffq69g5f1v"), TagName("tag1"), "invalid path")),
        (TagUnsafeV218(TagId("01arz3ndektsv4rrffq69g5f2v"), TagName("tag2"), "valid-path"))
      )

      when(mockTagRepository.getAll()).thenReturn(Monad[ConnectionIO].pure(testTags))
      when(mockTagRepository.bulkUpsert(any[List[TagUnsafeV218]])).thenReturn(Monad[ConnectionIO].pure(2))
      when(mockExecuter.transact(any[ConnectionIO[Any]])).thenReturn(
        IO.pure(testTags.asInstanceOf[Any]),
        IO.pure(2.asInstanceOf[Any])
      )

      runTagMigration(mockTagRepository, logger, mockExecuter).unsafeRunSync()

      verify(mockTagRepository).getAll()
      verify(mockTagRepository).bulkUpsert(any[List[TagUnsafeV218]])
      verify(mockExecuter, times(2)).transact(any[ConnectionIO[Any]])
    }

    "handle empty tag list" in {
      val mockTagRepository = mock(classOf[TagRepositoryV217[ConnectionIO]])
      val mockExecuter = mock(classOf[Executer[ConnectionIO, IO]])
      val emptyTags = Seq.empty[(Int, TagUnsafeV218)]

      when(mockTagRepository.getAll()).thenReturn(Monad[ConnectionIO].pure(emptyTags))
      when(mockTagRepository.bulkUpsert(any[List[TagUnsafeV218]])).thenReturn(Monad[ConnectionIO].pure(0))
      when(mockExecuter.transact(any[ConnectionIO[Any]])).thenReturn(
        IO.pure(emptyTags.asInstanceOf[Any]),
        IO.pure(0.asInstanceOf[Any])
      )

      runTagMigration(mockTagRepository, logger, mockExecuter).unsafeRunSync()

      verify(mockTagRepository).getAll()
      verify(mockTagRepository).bulkUpsert(any[List[TagUnsafeV218]])
      verify(mockExecuter, times(2)).transact(any[ConnectionIO[Any]])
    }

  }

  "runSeriesMigration" should {

    "successfully migrate series with valid paths" in {
      val mockSeriesRepository = mock(classOf[SeriesRepositoryV217[ConnectionIO]])
      val mockExecuter = mock(classOf[Executer[ConnectionIO, IO]])
      val testSeries = Seq(
        (SeriesUnsafeV218(SeriesId("01arz3ndektsv4rrffq69g5f1v"), SeriesName("series1"), "valid-path")),
        (SeriesUnsafeV218(SeriesId("01arz3ndektsv4rrffq69g5f2v"), SeriesName("series2"), "another-valid"))
      )

      when(mockSeriesRepository.getAll()).thenReturn(Monad[ConnectionIO].pure(testSeries))
      when(mockSeriesRepository.bulkUpsert(any[List[SeriesUnsafeV218]])).thenReturn(Monad[ConnectionIO].pure(2))
      when(mockExecuter.transact(any[ConnectionIO[Any]])).thenReturn(
        IO.pure(testSeries.asInstanceOf[Any]),
        IO.pure(2.asInstanceOf[Any])
      )

      runSeriesMigration(mockSeriesRepository, logger, mockExecuter).unsafeRunSync()

      verify(mockSeriesRepository).getAll()
      verify(mockSeriesRepository).bulkUpsert(any[List[SeriesUnsafeV218]])
      verify(mockExecuter, times(2)).transact(any[ConnectionIO[Any]])
    }

    "handle series with invalid paths" in {
      val mockSeriesRepository = mock(classOf[SeriesRepositoryV217[ConnectionIO]])
      val mockExecuter = mock(classOf[Executer[ConnectionIO, IO]])
      val testSeries = Seq(
        (SeriesUnsafeV218(SeriesId("01arz3ndektsv4rrffq69g5f1v"), SeriesName("series1"), "invalid path")),
        (SeriesUnsafeV218(SeriesId("01arz3ndektsv4rrffq69g5f2v"), SeriesName("series2"), "valid-path"))
      )

      when(mockSeriesRepository.getAll()).thenReturn(Monad[ConnectionIO].pure(testSeries))
      when(mockSeriesRepository.bulkUpsert(any[List[SeriesUnsafeV218]])).thenReturn(Monad[ConnectionIO].pure(2))
      when(mockExecuter.transact(any[ConnectionIO[Any]])).thenReturn(
        IO.pure(testSeries.asInstanceOf[Any]),
        IO.pure(2.asInstanceOf[Any])
      )

      runSeriesMigration(mockSeriesRepository, logger, mockExecuter).unsafeRunSync()

      verify(mockSeriesRepository).getAll()
      verify(mockSeriesRepository).bulkUpsert(any[List[SeriesUnsafeV218]])
      verify(mockExecuter, times(2)).transact(any[ConnectionIO[Any]])
    }

    "handle empty series list" in {
      val mockSeriesRepository = mock(classOf[SeriesRepositoryV217[ConnectionIO]])
      val mockExecuter = mock(classOf[Executer[ConnectionIO, IO]])
      val emptySeries = Seq.empty[SeriesUnsafeV218]

      when(mockSeriesRepository.getAll()).thenReturn(Monad[ConnectionIO].pure(emptySeries))
      when(mockSeriesRepository.bulkUpsert(any[List[SeriesUnsafeV218]])).thenReturn(Monad[ConnectionIO].pure(0))
      when(mockExecuter.transact(any[ConnectionIO[Any]])).thenReturn(
        IO.pure(emptySeries.asInstanceOf[Any]),
        IO.pure(0.asInstanceOf[Any])
      )

      runSeriesMigration(mockSeriesRepository, logger, mockExecuter).unsafeRunSync()

      verify(mockSeriesRepository).getAll()
      verify(mockSeriesRepository).bulkUpsert(any[List[SeriesUnsafeV218]])
      verify(mockExecuter, times(2)).transact(any[ConnectionIO[Any]])
    }

  }

}
