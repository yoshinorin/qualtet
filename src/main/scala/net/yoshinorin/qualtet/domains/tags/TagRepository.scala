package net.yoshinorin.qualtet.domains.tags

import doobie.ConnectionIO
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker

object TagRepository extends ConnectionIOFaker {

  def dispatch[T](request: TagRepositoryRequest[T]): ConnectionIO[T] = request match {
    case GetAll() => TagQuery.getAll.to[Seq]
    case FindById(id) => TagQuery.findById(id).option
    case FindByName(tagName) => TagQuery.findByName(tagName).option
    case BulkUpsert(data) =>
      data match {
        case None => ConnectionIOWithInt
        case Some(x) =>
          TagQuery.bulkUpsert.updateMany(x)
      }
    // TODO: fix return type `Int` to `Unit`
    case Delete(id) => TagQuery.delete(id).option.map(_ => 0)
  }

}
