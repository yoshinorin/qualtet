package net.yoshinorin.qualtet.infrastructure.versions

import cats.Monad
import cats.Eq
import cats.implicits.*
import cats.effect.IO
import net.yoshinorin.qualtet.infrastructure.db.Executer

import java.time.ZonedDateTime

class VersionService[F[_]: Monad](
  versionRepositoryAdapter: VersionRepositoryAdapter[F],
  applicationVersions: Option[ApplicationVersion[IO]] = None // TODO: `Option` to `List`
)(using executer: Executer[F, IO]) {

  def get: IO[Seq[Version]] = {
    executer.transact(versionRepositoryAdapter.get)
  }

  def createOrUpdate(data: Version): IO[Version] = {
    for {
      _ <- executer.transact(executer.defer(versionRepositoryAdapter.upsert(data)))
      versions <- this.get
    } yield (versions.filter(v => v.version === data.version).head)
  }

  def migrateIfNeed(): IO[Version] = {
    // TODO: logging
    for {
      data <- applicationVersions.get.get()
      maybeVersions <- this.get
      version <- maybeVersions.filter(v => v.version === data.version).headOption match {
        case None => this.createOrUpdate(data)
        case Some(v) => IO(v)
      }
      result <- version.migrationStatus match {
        case MigrationStatus.NOT_REQUIRED if version.deployedAt =!= 0 =>
          IO(version)
        case MigrationStatus.NOT_REQUIRED =>
          this.createOrUpdate(data.copy(deployedAt = ZonedDateTime.now.toEpochSecond))
        case MigrationStatus.UNAPPLIED | MigrationStatus.FAILED => {
          (for {
            _ <- this.createOrUpdate(data.copy(migrationStatus = MigrationStatus.IN_PROGRESS))
            _ <- applicationVersions.get.migrate()
            result <- this.createOrUpdate(data.copy(migrationStatus = MigrationStatus.SUCCESS, deployedAt = ZonedDateTime.now.toEpochSecond))
          } yield (result)).handleErrorWith { _ =>
            this.createOrUpdate(data.copy(migrationStatus = MigrationStatus.FAILED))
          }
        }
        case MigrationStatus.IN_PROGRESS | MigrationStatus.SUCCESS =>
          IO(version)
      }
    } yield (result)
  }

}
