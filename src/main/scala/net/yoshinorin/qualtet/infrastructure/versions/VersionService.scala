package net.yoshinorin.qualtet.infrastructure.versions

import cats.Monad
import cats.implicits.*
import cats.effect.IO
import net.yoshinorin.qualtet.infrastructure.db.Executer
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import java.time.ZonedDateTime

class VersionService[F[_]: Monad](
  versionRepositoryAdapter: VersionRepositoryAdapter[F]
)(using executer: Executer[F, IO], loggerFactory: Log4CatsLoggerFactory[IO]) {

  private val logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  def get: IO[Seq[Version]] = {
    executer.transact(versionRepositoryAdapter.get)
  }

  def createOrUpdate(data: Version): IO[Version] = {
    for {
      _ <- executer.transact(executer.defer(versionRepositoryAdapter.upsert(data)))
      versions <- this.get
    } yield (versions.filter(v => v.version === data.version).head)
  }

  // TODO: change the function scope to `private`.
  def migrateIfNeed(applicationVersion: ApplicationVersion[IO]): IO[Version] = {
    for {
      data <- applicationVersion.get()
      _ <- logger.info(s"Starting migration check for version: ${data.version.value}")
      maybeVersions <- this.get
      version <- maybeVersions.filter(v => v.version === data.version).headOption match {
        case None =>
          logger.info(s"Version ${data.version.value} not found in database, creating new record") *>
            this.createOrUpdate(data)
        case Some(v) =>
          logger.info(s"Version ${data.version.value} found in database with status: ${v.migrationStatus.value}") *>
            IO(v)
      }
      result <- version.migrationStatus match {
        case MigrationStatus.NOT_REQUIRED if version.deployedAt =!= 0 =>
          logger.info(s"Version ${data.version.value} migration not required, already deployed at: ${version.deployedAt}") *>
            IO(version)
        case MigrationStatus.NOT_REQUIRED =>
          logger.info(s"Version ${data.version.value} migration not required, updating deployment timestamp") *>
            this.createOrUpdate(data.copy(deployedAt = ZonedDateTime.now.toEpochSecond))
        case MigrationStatus.UNAPPLIED | MigrationStatus.FAILED => {
          logger.info(s"Starting migration for version ${data.version.value} with status: ${version.migrationStatus.value}") *>
            (for {
              _ <- logger.info(s"Setting migration status to in_progress for version: ${data.version.value}")
              _ <- this.createOrUpdate(data.copy(migrationStatus = MigrationStatus.IN_PROGRESS))
              _ <- logger.info(s"Executing migration for version: ${data.version.value}")
              _ <- applicationVersion.migrate()
              _ <- logger.info(s"Migration completed successfully for version: ${data.version.value}")
              result <- this.createOrUpdate(data.copy(migrationStatus = MigrationStatus.SUCCESS, deployedAt = ZonedDateTime.now.toEpochSecond))
              _ <- logger.info(s"Version ${data.version.value} migration status updated to success")
            } yield (result)).handleErrorWith { error =>
              logger.error(error)(s"Migration failed for version ${data.version.value}: ${error.getMessage}") *>
                this.createOrUpdate(data.copy(migrationStatus = MigrationStatus.FAILED))
            }
        }
        case MigrationStatus.IN_PROGRESS | MigrationStatus.SUCCESS =>
          logger.info(s"Version ${data.version.value} migration skipped, current status: ${version.migrationStatus.value}") *>
            IO(version)
      }
      _ <- logger.info(s"Migration check completed for version: ${result.version.value} with final status: ${result.migrationStatus.value}")
    } yield (result)
  }

  def migrate(applicationVersion: Option[ApplicationVersion[IO]] = None): IO[Version] = {
    this.migrateIfNeed(applicationVersion.get)
  }

}
