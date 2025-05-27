package net.yoshinorin.qualtet.infrastructure.versions

import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieExecuter
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.infrastructure.versions.VersionServiceSpec
class VersionServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  given doobieExecuterContext: DoobieExecuter = new DoobieExecuter(fixtureTx)

  "VersionServiceSpec" should {

    "create and update version" in {

      val newVersion: Version = Version(version = "0.0.0", migrationStatus = MigrationStatus.NOT_REQUIRED, deployedAt = 0)
      val updatedVersion: Version = Version(version = "0.0.1", migrationStatus = MigrationStatus.SUCCESS, deployedAt = 1748126398)
      val anotherVersion: Version = Version(version = "0.1.0", migrationStatus = MigrationStatus.UNAPPLIED, deployedAt = 1748126397)

      (for {
        craeted <- versionService.createOrUpdate(newVersion)
        updated <- versionService.createOrUpdate(updatedVersion)
        craetedAnother <- versionService.createOrUpdate(anotherVersion)
      } yield {
        assert(craeted.version === "0.0.0")
        assert(craeted.migrationStatus === MigrationStatus.NOT_REQUIRED)
        assert(craeted.deployedAt === 0)

        assert(updated.version === "0.0.1")
        assert(updated.migrationStatus === MigrationStatus.SUCCESS)
        assert(updated.deployedAt === 1748126398)

        assert(craetedAnother.version === "0.1.0")
        assert(craetedAnother.migrationStatus === MigrationStatus.UNAPPLIED)
        assert(craetedAnother.deployedAt === 1748126397)
      }).unsafeRunSync()

    }

  }

}
