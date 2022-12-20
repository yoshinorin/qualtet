package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.domains.contents.{ContentId, Path}

trait ContentRepository[M[_]] {
  def upsert(data: Content): M[Int]
  def findById(id: ContentId): M[Option[Content]]
  def findByPath(path: Path): M[Option[Content]]
  def findByPathWithMeta(path: Path): M[Option[ResponseContentDbRow]]
  def delete(id: ContentId): M[Unit]
}
