package net.yoshinorin.qualtet.infrastructure.db

import net.yoshinorin.qualtet.fixture.Fixture.{contentTypeService, migrator}
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.infrastructure.db.MigratorSpec
class MigratorSpec extends AnyWordSpec {

  "Migrator" should {

    "migrate" in {

      migrator.migrate(contentTypeService).unsafeRunSync()

      val result = (for {
        a <- contentTypeService.findByName("article")
        p <- contentTypeService.findByName("page")
      } yield (a, p)).unsafeRunSync()

      assert(result._1.isDefined)
      assert(result._2.isDefined)
    }

  }

}
