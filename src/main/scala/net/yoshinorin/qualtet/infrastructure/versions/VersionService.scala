package net.yoshinorin.qualtet.infrastructure.versions

import cats.Monad
import cats.implicits.*
import cats.effect.IO
import net.yoshinorin.qualtet.infrastructure.db.Executer

class VersionService[F[_]: Monad](
  versionRepositoryAdapter: VersionRepositoryAdapter[F]
)(using
  executer: Executer[F, IO]
) {

  def get: IO[Seq[Version]] = {
    executer.transact(versionRepositoryAdapter.get)
  }

  def createOrUpdate(data: Version): IO[Version] = {
    for {
      _ <- executer.transact(executer.defer(versionRepositoryAdapter.upsert(data)))
      versions <- this.get
    } yield (versions.filter(v => v.version === data.version).head)
  }
}
