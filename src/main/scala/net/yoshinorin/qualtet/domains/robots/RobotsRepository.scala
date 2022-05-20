package net.yoshinorin.qualtet.domains.robots

import doobie.ConnectionIO

object RobotsRepository {

  def dispatch[T](request: RobotsRepositoryRequest[T]): ConnectionIO[T] = request match {
    case Upsert(data) => RobotsQuery.upsert.run(data)
  }

}
