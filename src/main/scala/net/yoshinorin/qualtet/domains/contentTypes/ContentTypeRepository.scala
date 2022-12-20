package net.yoshinorin.qualtet.domains.contentTypes

import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

trait ContentTypeRepository[M[_]] {
  def upsert(data: ContentType): M[Int]
  def getAll(): M[Seq[ContentType]]
  def findByName(name: String): M[Option[ContentType]]
}
