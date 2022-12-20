package net.yoshinorin.qualtet.domains.robots

import doobie.Write
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.contents.ContentId

trait RobotsRepository[M[_]] {
  def upsert(data: Robots): M[Int]
  def delete(contentId: ContentId): M[Unit]
}

class DoobieRobotsRepository extends RobotsRepository[ConnectionIO] {

  implicit val robotsWrite: Write[Robots] =
    Write[(String, String)].contramap(p => (p.contentId.value, p.attributes.value))

  override def upsert(data: Robots): ConnectionIO[Int] = RobotsQuery.upsert.run(data)
  override def delete(contentId: ContentId): ConnectionIO[Unit] = RobotsQuery.delete(contentId).option.map(_ => ())
}
