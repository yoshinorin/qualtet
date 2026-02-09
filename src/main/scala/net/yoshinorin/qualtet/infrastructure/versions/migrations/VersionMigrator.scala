package net.yoshinorin.qualtet.infrastructure.versions

import cats.Monad
import net.yoshinorin.qualtet.infrastructure.db.Executer

import scala.annotation.nowarn

trait VersionMigrator[F[_]: Monad, G[_]: Monad @nowarn](init: Version) {
  def migrate()(using executer: Executer[F, G]): F[Unit]
  def get(): F[Version]
  def getInit(): F[Version] = Monad[F].pure(init)
}

object VersionMigrator {

  def instance[F[_]: Monad, G[_]: Monad](
    initVersion: Version,
    migrateFunc: () => F[Unit]
  ): VersionMigrator[F, G] = {
    new VersionMigrator[F, G](init = initVersion) {
      override def migrate()(using executer: Executer[F, G]): F[Unit] = migrateFunc()
      override def get(): F[Version] = super.getInit()
      override def getInit(): F[Version] = super.getInit()
    }
  }

}
