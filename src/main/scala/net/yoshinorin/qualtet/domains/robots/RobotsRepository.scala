package net.yoshinorin.qualtet.domains.robots

import net.yoshinorin.qualtet.domains.contents.ContentId

trait RobotsRepository[F[_]] {
  def upsert(data: Robots): F[Int]
  def delete(contentId: ContentId): F[Unit]
}
