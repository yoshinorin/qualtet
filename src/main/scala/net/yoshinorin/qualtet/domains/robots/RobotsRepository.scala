package net.yoshinorin.qualtet.domains.robots

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.contents.ContentId

trait RobotsRepository[M[_]] {
  def upsert(data: Robots): M[Int]
  def delete(contentId: ContentId): M[Unit]
}

class DoobieRobotsRepository extends RobotsRepository[ConnectionIO] {
  override def upsert(data: Robots): ConnectionIO[Int] = RobotsQuery.upsert.run(data)
  override def delete(contentId: ContentId): ConnectionIO[Unit] = RobotsQuery.delete(contentId).option.map(_ => 0)
}
