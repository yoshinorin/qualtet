package net.yoshinorin.qualtet.infrastructure.versions

import cats.Monad
import net.yoshinorin.qualtet.infrastructure.db.Executer

trait VersionMigrator[M[_]: Monad, F[_]: Monad](init: Version) {
  def migrate()(using executer: Executer[M, F]): F[Unit]
  def get(): F[Version]
  def getInit(): F[Version] = Monad[F].pure(init)
}

object VersionMigrator {

  def instance[M[_]: Monad, F[_]: Monad](
    initVersion: Version,
    migrateFunc: () => F[Unit]
  ): VersionMigrator[M, F] = {
    new VersionMigrator[M, F](init = initVersion) {
      override def migrate()(using executer: Executer[M, F]): F[Unit] = migrateFunc()
      override def get(): F[Version] = super.getInit()
      override def getInit(): F[Version] = super.getInit()
    }
  }

}
