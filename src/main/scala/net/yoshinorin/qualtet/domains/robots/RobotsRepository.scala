package net.yoshinorin.qualtet.domains.robots

import net.yoshinorin.qualtet.domains.contents.ContentId

trait RobotsRepository[M[_]] {
  def upsert(data: Robots): M[Int]
  def delete(contentId: ContentId): M[Unit]
}
