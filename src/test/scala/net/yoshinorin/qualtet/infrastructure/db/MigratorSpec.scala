package net.yoshinorin.qualtet.infrastructure.db

import net.yoshinorin.qualtet.fixture.Fixture.contentTypeService
import net.yoshinorin.qualtet.Modules
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieTransactor
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.infrastructure.db.MigratorSpec
class MigratorSpec extends AnyWordSpec {

  given dbContext: DoobieTransactor = new DoobieTransactor(Modules.config.db)

  "Migrator" should {

    "be migrate" in {

      Modules.migrator.migrate(contentTypeService).unsafeRunSync()

      val result = (for {
        a <- contentTypeService.findByName("article")
        p <- contentTypeService.findByName("page")
      } yield (a, p)).unsafeRunSync()

      assert(result._1.isDefined)
      assert(result._2.isDefined)
    }

  }

}
