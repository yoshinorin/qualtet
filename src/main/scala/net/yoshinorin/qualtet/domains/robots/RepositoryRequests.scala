package net.yoshinorin.qualtet.domains.robots

import net.yoshinorin.qualtet.domains.repository.requests._

trait RobotsRepositoryRequest[T] extends RepositoryRequest[T]
final case class Upsert(data: Robots) extends RobotsRepositoryRequest[Int]
