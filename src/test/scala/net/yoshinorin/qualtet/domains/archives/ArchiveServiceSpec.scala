package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.ArchiveServiceSpec
class ArchiveServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContentRequestModels(40, "archives").unsafeCreateConternt()
  }

  "ArchiveService" should {
    "get" in {
      val result = archiveService.get.unsafeRunSync()
      assert(result.size >= 39)
    }

  }

}
