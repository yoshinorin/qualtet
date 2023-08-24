package net.yoshinorin.qualtet.domains.contentTypes

trait ContentTypeRepository[M[_]] {
  def upsert(data: ContentType): M[Int]
  def getAll(): M[Seq[ContentType]]
  def findByName(name: String): M[Option[ContentType]]
}
