package net.yoshinorin.qualtet.domains.robots

import net.yoshinorin.qualtet.domains.contents.ContentId

trait RobotsRepository[F[_]] {
  def upsert(data: RobotsWriteModel): F[Int]
  def delete(contentId: ContentId): F[Unit]
}

object RobotsRepository {

  import org.typelevel.doobie.Write
  import org.typelevel.doobie.ConnectionIO

  given RobotsRepository: RobotsRepository[ConnectionIO] = {
    new RobotsRepository[ConnectionIO] {

      given robotsWrite: Write[RobotsWriteModel] =
        Write[(String, String)].contramap(p => (p.contentId.value, p.attributes.value))

      override def upsert(data: RobotsWriteModel): ConnectionIO[Int] = RobotsQuery.upsert.run(data)
      override def delete(contentId: ContentId): ConnectionIO[Unit] = RobotsQuery.delete(contentId).run.map(_ => ())

    }
  }

}
