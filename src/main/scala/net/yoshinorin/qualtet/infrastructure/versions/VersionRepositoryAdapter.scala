package net.yoshinorin.qualtet.infrastructure.versions

import cats.Monad
import cats.data.ContT
import cats.implicits.*

class VersionRepositoryAdapter[F[_]: Monad](
  versionRepository: VersionRepository[F]
) {

  private[versions] def upsert(data: Version): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { _ =>
      val w = VersionWriteModel(
        version = data.version.value,
        migrationStatus = data.migrationStatus,
        deployedAt = data.deployedAt
      )
      versionRepository.upsert(w)
    }
  }

  private[versions] def get: ContT[F, Seq[Version], Seq[Version]] = {
    ContT.apply[F, Seq[Version], Seq[Version]] { _ =>
      versionRepository.get.map { version =>
        version.map(v =>
          Version(
            // TODO: use `unsafe` in `Repository`
            version = VersionString.unsafe(v.version),
            migrationStatus = v.migrationStatus,
            deployedAt = v.deployedAt
          )
        )
      }
    }
  }

}
