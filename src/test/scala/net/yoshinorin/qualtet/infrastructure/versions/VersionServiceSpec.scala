package net.yoshinorin.qualtet.infrastructure.versions

import java.time.ZonedDateTime
import cats.Monad
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieExecuter
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.infrastructure.versions.VersionServiceSpec
class VersionServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  given doobieExecuterContext: DoobieExecuter = new DoobieExecuter(fixtureTx)

  "CreateOrUpdate" should {

    "create and update version" in {

      val newVersion: Version = Version(version = VersionString("0.0.0"), migrationStatus = MigrationStatus.NOT_REQUIRED, deployedAt = 0)
      val updatedVersion: Version = Version(version = VersionString("0.0.1"), migrationStatus = MigrationStatus.SUCCESS, deployedAt = 1748126398)
      val anotherVersion: Version = Version(version = VersionString("0.1.0"), migrationStatus = MigrationStatus.UNAPPLIED, deployedAt = 1748126397)

      (for {
        craeted <- versionService.createOrUpdate(newVersion)
        updated <- versionService.createOrUpdate(updatedVersion)
        craetedAnother <- versionService.createOrUpdate(anotherVersion)
      } yield {
        assert(craeted.version === VersionString("0.0.0"))
        assert(craeted.migrationStatus === MigrationStatus.NOT_REQUIRED)
        assert(craeted.deployedAt === 0)

        assert(updated.version === VersionString("0.0.1"))
        assert(updated.migrationStatus === MigrationStatus.SUCCESS)
        assert(updated.deployedAt === 1748126398)

        assert(craetedAnother.version === VersionString("0.1.0"))
        assert(craetedAnother.migrationStatus === MigrationStatus.UNAPPLIED)
        assert(craetedAnother.deployedAt === 1748126397)
      }).unsafeRunSync()

    }
  }

  "migrate" should {
    import net.yoshinorin.qualtet.infrastructure.versions.ApplicationVersion
    import cats.effect.IO

    def createInstance[F[_]: Monad](
      defaultVersion: Version,
      migrateFunc: () => F[Unit]
    ): ApplicationVersion[F] = {
      new ApplicationVersion[F](default = defaultVersion) {
        override def migrate(): F[Unit] = migrateFunc()
        override def get(): F[Version] = super.getDefault()
        override def getDefault(): F[Version] = super.getDefault()
      }
    }

    val now = ZonedDateTime.now.toEpochSecond
    val versionService = new VersionService(versionRepositoryAdapter)

    "insert new record with `not_required`" in {

      val v0000Default: Version = Version(version = VersionString("0.0.0.0"), migrationStatus = MigrationStatus.NOT_REQUIRED, deployedAt = 0)
      given V0000: ApplicationVersion[IO] = {
        createInstance[IO](v0000Default, () => IO.pure(()))
      }
      val v0000: ApplicationVersion[IO] = summon[ApplicationVersion[IO]](using V0000)

      (for {
        craeted <- versionService.migrate(Some(v0000))
        // NOTE: Rollback to default instead of delete from DB
        // TODO: delete from DB
        _ <- versionService.createOrUpdate(craeted.copy(deployedAt = 0))
      } yield {
        assert(craeted.version === "0.0.0.0")
        assert(craeted.migrationStatus === MigrationStatus.NOT_REQUIRED)
        assert(craeted.deployedAt >= now)
      }).unsafeRunSync()
    }

    "skip migration if `deployedAt` is not `0`" in {

      val v0001Default: Version = Version(version = VersionString("0.0.0.1"), migrationStatus = MigrationStatus.NOT_REQUIRED, deployedAt = 1749136951)
      given V0001: ApplicationVersion[IO] = {
        createInstance[IO](v0001Default, () => IO.pure(()))
      }
      val v0001: ApplicationVersion[IO] = summon[ApplicationVersion[IO]](using V0001)

      (for {
        _ <- versionService.createOrUpdate(v0001Default)
        migrated <- versionService.migrate(Some(v0001))
      } yield {
        assert(migrated.version === VersionString("0.0.0.1"))
        assert(migrated.migrationStatus === MigrationStatus.NOT_REQUIRED)
        assert(migrated.deployedAt === v0001Default.deployedAt)
      }).unsafeRunSync()
    }

    "try migration if status is `unapplied`" in {

      val v0004Default: Version = Version(version = VersionString("0.0.0.4"), migrationStatus = MigrationStatus.UNAPPLIED, deployedAt = 0)
      given V0004: ApplicationVersion[IO] = {
        createInstance[IO](v0004Default, () => IO.pure(()))
      }
      val v0004: ApplicationVersion[IO] = summon[ApplicationVersion[IO]](using V0004)

      (for {
        migrated <- versionService.migrate(Some(v0004))
        // NOTE: Rollback to default instead of delete from DB
        // TODO: delete from DB
        _ <- versionService.createOrUpdate(migrated.copy(migrationStatus = MigrationStatus.UNAPPLIED, deployedAt = 0))
      } yield {
        assert(migrated.version === VersionString("0.0.0.4"))
        assert(migrated.migrationStatus === MigrationStatus.SUCCESS)
        assert(migrated.deployedAt >= now)
      }).unsafeRunSync()
    }

    "try migration if status is `failed`" in {

      val v0005Default: Version = Version(version = VersionString("0.0.0.5"), migrationStatus = MigrationStatus.FAILED, deployedAt = 0)
      given V0005: ApplicationVersion[IO] = {
        createInstance[IO](v0005Default, () => IO.pure(()))
      }
      val v0005: ApplicationVersion[IO] = summon[ApplicationVersion[IO]](using V0005)

      (for {
        migrated <- versionService.migrate(Some(v0005))
        // NOTE: Rollback to default instead of delete from DB
        // TODO: delete from DB
        _ <- versionService.createOrUpdate(migrated.copy(migrationStatus = MigrationStatus.FAILED, deployedAt = 0))
      } yield {
        assert(migrated.version === VersionString("0.0.0.5"))
        assert(migrated.migrationStatus === MigrationStatus.SUCCESS)
        assert(migrated.deployedAt >= now)
      }).unsafeRunSync()
    }

    "failed migration" in {

      val v0006Default: Version = Version(version = VersionString("0.0.0.6"), migrationStatus = MigrationStatus.UNAPPLIED, deployedAt = 0)
      given V0006: ApplicationVersion[IO] = {
        createInstance[IO](
          v0006Default,
          () =>
            IO {
              throw new RuntimeException("something went wrong")
            }
        )
      }
      val v0006: ApplicationVersion[IO] = summon[ApplicationVersion[IO]](using V0006)

      (for {
        migrated <- versionService.migrate(Some(v0006))
        // NOTE: Rollback to default instead of delete from DB
        // TODO: delete from DB
        _ <- versionService.createOrUpdate(v0006Default)
      } yield {
        assert(migrated.version === VersionString("0.0.0.6"))
        assert(migrated.migrationStatus === MigrationStatus.FAILED)
        assert(migrated.deployedAt === 0)
      }).unsafeRunSync()
    }

    "skip migration if status is `in_progress`" in {

      val v0002Default: Version = Version(version = VersionString("0.0.0.2"), migrationStatus = MigrationStatus.IN_PROGRESS, deployedAt = 0)
      given V0002: ApplicationVersion[IO] = {
        createInstance[IO](v0002Default, () => IO.pure(()))
      }
      val v0002: ApplicationVersion[IO] = summon[ApplicationVersion[IO]](using V0002)

      (for {
        _ <- versionService.createOrUpdate(v0002Default)
        migrated <- versionService.migrate(Some(v0002))
      } yield {
        assert(migrated.version === VersionString("0.0.0.2"))
        assert(migrated.migrationStatus === MigrationStatus.IN_PROGRESS)
        assert(migrated.deployedAt === v0002Default.deployedAt)
      }).unsafeRunSync()
    }

    "skip migration if status is `success`" in {

      val v0003Default: Version = Version(version = VersionString("0.0.0.3"), migrationStatus = MigrationStatus.SUCCESS, deployedAt = 1749137824)
      given V0003: ApplicationVersion[IO] = {
        createInstance[IO](v0003Default, () => IO.pure(()))
      }
      val v0003: ApplicationVersion[IO] = summon[ApplicationVersion[IO]](using V0003)

      (for {
        _ <- versionService.createOrUpdate(v0003Default)
        migrated <- versionService.migrate(Some(v0003))
      } yield {
        assert(migrated.version === VersionString("0.0.0.3"))
        assert(migrated.migrationStatus === MigrationStatus.SUCCESS)
        assert(migrated.deployedAt === v0003Default.deployedAt)
      }).unsafeRunSync()
    }
  }

}
