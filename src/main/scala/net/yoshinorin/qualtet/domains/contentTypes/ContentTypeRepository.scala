package net.yoshinorin.qualtet.domains.contentTypes

trait ContentTypeRepository[F[_]] {
  def upsert(data: ContentType): F[Int]
  def getAll(): F[Seq[ContentType]]
  def findByName(name: String): F[Option[ContentType]]
}
