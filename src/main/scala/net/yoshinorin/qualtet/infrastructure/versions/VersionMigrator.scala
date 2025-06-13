package net.yoshinorin.qualtet.infrastructure.versions

import cats.Monad
import net.yoshinorin.qualtet.infrastructure.db.Executer

trait VersionMigrator[M[_]: Monad, F[_]: Monad](default: Version) {
  def migrate()(using executer: Executer[M, F]): F[Unit]
  def get(): F[Version]
  def getDefault(): F[Version] = Monad[F].pure(default)
}
