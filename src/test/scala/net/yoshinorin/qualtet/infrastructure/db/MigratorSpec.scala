package net.yoshinorin.qualtet.infrastructure.db

import net.yoshinorin.qualtet.fixture.unsafe
import cats.effect.IO
import net.yoshinorin.qualtet.fixture.Fixture.{contentTypeService, flywayMigrator, migrator}
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeName
import org.scalatest.wordspec.AnyWordSpec

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.infrastructure.db.MigratorSpec
class MigratorSpec extends AnyWordSpec {

  "Migrator" should {

    "flywayMigrator and migrate" in {

      (for {
        _ <- IO(flywayMigrator.migrate())
        _ <- migrator.migrate(contentTypeService)
      } yield ()).unsafeRunSync()

      val result = (for {
        a <- contentTypeService.findByName(ContentTypeName("article").unsafe)
        p <- contentTypeService.findByName(ContentTypeName("page").unsafe)
      } yield (a, p)).unsafeRunSync()

      assert(result._1.isDefined)
      assert(result._2.isDefined)
    }

  }

}
