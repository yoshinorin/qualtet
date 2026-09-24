package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.domains.period.{From, Period, To}
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.fixture.unsafe

import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.ArchiveServiceSpec
class ArchiveServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContentRequestModels(40, "archives").unsafeCreateConternt()
  }

  "ArchiveService" should {
    "get" in {
      val result = archiveService.get(Seq()).unsafeRunSync().unsafe
      assert(result.size >= 39)
    }

    "get with an open ended published period" in {
      val result = archiveService.get(Seq(Period.Published(Some(From(1)), None))).unsafeRunSync().unsafe
      assert(result.size >= 39)
    }

    "get with an open beginning published period" in {
      val now = java.time.Instant.now().getEpochSecond
      val result = archiveService.get(Seq(Period.Published(None, Some(To(now + 3600))))).unsafeRunSync().unsafe
      assert(result.size >= 39)
    }

    "get nothing if the published period matches no content" in {
      val result = archiveService.get(Seq(Period.Published(Some(From(1)), Some(To(2))))).unsafeRunSync().unsafe
      assert(result.isEmpty)
    }

    "get nothing if the updated period matches no content" in {
      val result = archiveService.get(Seq(Period.Updated(Some(From(1)), Some(To(2))))).unsafeRunSync().unsafe
      assert(result.isEmpty)
    }

    "get nothing if published and updated periods do not overlap" in {
      val now = java.time.Instant.now().getEpochSecond
      val result = archiveService
        .get(
          Seq(
            Period.Published(Some(From(1)), Some(To(now + 3600))),
            Period.Updated(Some(From(1)), Some(To(2)))
          )
        )
        .unsafeRunSync()
        .unsafe
      assert(result.isEmpty)
    }

  }

}
