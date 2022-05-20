package net.yoshinorin.qualtet.domains.externalResources

import doobie.ConnectionIO
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker

object ExternalResourceRepository extends ConnectionIOFaker {

  def dispatch[T](request: ExternalResourceRepositoryRequest[T]): ConnectionIO[T] = request match {
    case BulkUpsert(data) => data match {
      case None => ConnectionIOWithInt
      case Some(x) =>
        ExternalResourceQuery.bulkUpsert.updateMany(x)
    }
  }
}
