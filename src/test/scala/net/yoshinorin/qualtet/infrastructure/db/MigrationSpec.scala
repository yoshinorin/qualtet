package net.yoshinorin.qualtet.infrastructure.db

import net.yoshinorin.qualtet.fixture.Fixture.contentTypeService
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.infrastructure.db.MigrationSpec
class MigrationSpec extends AnyWordSpec {

  "Migration" should {

    "be migrate" in {
      Migration.migrate(contentTypeService)

      val a = contentTypeService.findByName("article").unsafeRunSync()
      assert(a.isDefined)

      val p = contentTypeService.findByName("page").unsafeRunSync()
      assert(p.isDefined)
    }

  }

}
