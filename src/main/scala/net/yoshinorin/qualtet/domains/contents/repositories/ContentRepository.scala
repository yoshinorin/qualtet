package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.domains.contents.{ContentId, Path}

trait ContentRepository[F[_]] {
  def upsert(data: Content): F[Int]
  def findById(id: ContentId): F[Option[Content]]
  def findByPath(path: Path): F[Option[Content]]
  def findByPathWithMeta(path: Path): F[Option[ReadContentDbRow]]
  def delete(id: ContentId): F[Unit]
}
