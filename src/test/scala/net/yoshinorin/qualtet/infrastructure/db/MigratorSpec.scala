package net.yoshinorin.qualtet.infrastructure.db

import net.yoshinorin.qualtet.fixture.Fixture.contentTypeService
import net.yoshinorin.qualtet.Modules
import org.scalatest.wordspec.AnyWordSpec
import cats.effect.unsafe.implicits.global
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

// testOnly net.yoshinorin.qualtet.infrastructure.db.MigratorSpec
class MigratorSpec extends AnyWordSpec {

  given dbContext: DoobieContext = new DoobieContext(Modules.config.db)

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
