package net.yoshinorin.qualtet.infrastructure.versions

import doobie.{Read, Write}
import doobie.syntax.all.toSqlInterpolator
import doobie.util.query.Query0
import doobie.util.update.Update

object VersionQuery {

  def upsert: Write[VersionWriteModel] ?=> Update[VersionWriteModel] = {
    val q = s"""
      INSERT INTO versions (version, migration_status, deployed_at)
        VALUES (?, ?, ?)
      ON DUPLICATE KEY UPDATE
        migration_status = VALUES(migration_status),
        deployed_at = VALUES(deployed_at)
    """
    Update[VersionWriteModel](q)
  }

  def getAll: Read[VersionReadModel] ?=> Query0[VersionReadModel] = {
    sql"SELECT version, migration_status, deployed_at FROM versions"
      .query[VersionReadModel]
  }

}
