package net.yoshinorin.qualtet.infrastructure.versions

trait VersionRepository[F[_]] {
  def get: F[Seq[VersionReadModel]]
  def upsert(data: VersionWriteModel): F[Int]
}

object VersionRepository {

  import doobie.{Read, Write}
  import doobie.ConnectionIO
  import net.yoshinorin.qualtet.syntax.*

  given VersionRepository: VersionRepository[ConnectionIO] = {
    new VersionRepository[ConnectionIO] {
      given versionRead: Read[VersionReadModel] =
        Read[(String, String, Long)].map { case (verstion, migrationStatus, deployedAt) =>
          VersionReadModel(verstion, MigrationStatus.valueOf(migrationStatus.toUpper), deployedAt)
        }

      given versionWrite: Write[VersionWriteModel] =
        Write[(String, String, Long)].contramap(v => (v.version, v.migrationStatus.toString().toLower, v.deployedAt))

      override def get: ConnectionIO[Seq[VersionReadModel]] = {
        VersionQuery.getAll.to[Seq]
      }

      override def upsert(data: VersionWriteModel): ConnectionIO[Int] = {
        VersionQuery.upsert.run(data)
      }
    }
  }

}
