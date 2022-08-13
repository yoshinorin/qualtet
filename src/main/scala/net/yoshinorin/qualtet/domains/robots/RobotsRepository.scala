package net.yoshinorin.qualtet.domains.robots

import doobie.ConnectionIO

object RobotsRepository {

  def dispatch[T](request: RobotsRepositoryRequest[T]): ConnectionIO[T] = request match {
    case Upsert(data) => RobotsQuery.upsert.run(data)
    // TODO: fix return type `Int` to `Unit`
    case Delete(content_id) => RobotsQuery.delete(content_id).option.map(_ => 0)
  }

}
