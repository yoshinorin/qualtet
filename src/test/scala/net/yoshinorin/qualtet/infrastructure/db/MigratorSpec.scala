package net.yoshinorin.qualtet.infrastructure.db

import net.yoshinorin.qualtet.fixture.Fixture.contentTypeService
import net.yoshinorin.qualtet.Modules
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.infrastructure.db.MigratorSpec
class MigratorSpec extends AnyWordSpec {

  "Migrator" should {

    "be migrate" in {
      Modules.migrator.migrate(contentTypeService)

      val a = contentTypeService.findByName("article").unsafeRunSync()
      assert(a.isDefined)

      val p = contentTypeService.findByName("page").unsafeRunSync()
      assert(p.isDefined)
    }

  }

}
