package net.yoshinorin.qualtet.domains.models.contents

import cats.effect.IO

trait ContentRepository {

  /**
   * create a content
   *
   * @param data Instance of Content
   * @return created Content
   */
  def insert(data: Content): IO[Content]

  def find = ???

  def findByPath(path: String): IO[Content]

  def update = ???

  def getAll: IO[Seq[Content]]
}
