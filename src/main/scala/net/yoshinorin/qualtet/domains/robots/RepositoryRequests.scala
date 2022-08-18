package net.yoshinorin.qualtet.domains.robots

import net.yoshinorin.qualtet.domains.repository.requests._
import net.yoshinorin.qualtet.domains.contents.ContentId

trait RobotsRepositoryRequest[T] extends RepositoryRequest[T] {
  def dispatch = RobotsRepository.dispatch(this)
}
final case class Upsert(data: Robots) extends RobotsRepositoryRequest[Int]
final case class Delete(content_id: ContentId) extends RobotsRepositoryRequest[Int]
