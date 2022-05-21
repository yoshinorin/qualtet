package net.yoshinorin.qualtet.domains.tags

import doobie.ConnectionIO
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker

object TagRepository extends ConnectionIOFaker {

  def dispatch[T](request: TagRepositoryRequest[T]): ConnectionIO[T] = request match {
    case GetAll() => TagQuery.getAll.to[Seq]
    case FindByName(tagName) => TagQuery.findByName(tagName).option
    case BulkUpsert(data) =>
      data match {
        case None => ConnectionIOWithInt
        case Some(x) =>
          TagQuery.bulkUpsert.updateMany(x)
      }
  }

}
