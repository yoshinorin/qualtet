package net.yoshinorin.qualtet.infrastructure.versions

import cats.Monad

// TODO: Refactor naming
trait ApplicationVersion[F[_]: Monad](default: Version) {
  def migrate(): F[Unit]
  def get(): F[Version]
  def getDefault(): F[Version] = Monad[F].pure(default)
}

